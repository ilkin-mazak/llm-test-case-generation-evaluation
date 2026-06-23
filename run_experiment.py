import os
import subprocess
import pandas as pd
import ollama
import re
import glob
import datetime
import shutil

# --- Configuration ---
MODELS = ["qwen2.5-coder:14b"]

TARGET_CLASSES = {
    "CharUtils": {
        "source": "src/main/java/org/apache/commons/lang3/CharUtils.java",
        "test_path": "src/test/java/org/apache/commons/lang3/LlmGeneratedCharUtilsTest.java",
        "package": "org.apache.commons.lang3"
    },
    "DateUtils": {
        "source": "src/main/java/org/apache/commons/lang3/time/DateUtils.java",
        "test_path": "src/test/java/org/apache/commons/lang3/time/LlmGeneratedDateUtilsTest.java",
        "package": "org.apache.commons.lang3.time"
    },
    "ReflectionToStringBuilder": {
        "source": "src/main/java/org/apache/commons/lang3/builder/ReflectionToStringBuilder.java",
        "test_path": "src/test/java/org/apache/commons/lang3/builder/LlmGeneratedReflectionToStringBuilderTest.java",
        "package": "org.apache.commons.lang3.builder"
    }
}

PROMPTS = {
    "Zero-Shot": """Write a JUnit 5 test class for the following Java class. Output only valid, executable Java code without markdown formatting or conversational text. 
Target Class Context:
{SOURCE_CODE}""",

    "Template-Driven": """<source_code>
{SOURCE_CODE}
</source_code>

INSTRUCTIONS:
You are an expert Java developer. Based on the <source_code> above, write a complete JUnit 5 test class.
You MUST follow these rules absolutely:
1. Output ONLY raw, valid Java code wrapped in ```java ... ```.
2. Use this exact skeleton. Do not change the class name.
3. EXHAUSTIVE COVERAGE: You MUST generate at least one @Test method for EVERY public method present in the <source_code>.
4. EDGE CASES: You MUST include specific assertions for null inputs, negative values, and boundary limits.
5. TYPE RULES: Handle boundary values properly by explicitly casting primitives (e.g., `(char) 128`).
6. Do NOT add any explanations or conversational text.

```java
package {PACKAGE_NAME};

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LlmGenerated{CLASS_NAME}Test {
    
    @Test
    public void testBasicFunctions() {
        // Write assertions here using proper Java types
    }
}
```""",

    "Iterative": """The previously generated test class failed compilation. Here is the exact error log from Maven:
{ERROR_LOG}

Analyze this failure and rewrite the test class to resolve the issue.
Output ONLY the corrected raw Java code wrapped in ```java ... ```.
Ensure you are using ONLY JUnit 5 imports (org.junit.jupiter.api)."""
}

results = []

# --- Output Directory Setup ---
timestamp = datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
output_dir = os.path.expanduser(f"~/Desktop/LLM_Thesis_Run_{timestamp}")
os.makedirs(output_dir, exist_ok=True)


def cleanup_old_tests():
    """Deletes previously generated LLM tests to prevent Maven cross-contamination."""
    print("   [System] Cleaning up old test files...")
    for file_path in glob.glob("src/test/java/org/apache/commons/lang3/**/LlmGenerated*Test.java", recursive=True):
        try:
            os.remove(file_path)
        except OSError:
            pass


def extract_java_code(text, expected_class_name):
    if "```java" in text:
        code = text.split("```java")[1].split("```")[0].strip()
    elif "```" in text:
        code = text.split("```")[1].split("```")[0].strip()
    else:
        code = text.strip()

    if not re.search(r'^(package|import|public\s+class)', code, re.IGNORECASE):
        return "HALLUCINATION_REJECTED"

    # Sanitation Step 1: Force correct class name
    code = re.sub(r'public\s+class\s+\w+', f'public class {expected_class_name}', code)

    # Sanitation Step 2: Force JUnit 5 imports if LLM hallucinates JUnit 4
    code = code.replace("import org.junit.Test;", "import org.junit.jupiter.api.Test;")
    code = code.replace("import static org.junit.Assert.", "import static org.junit.jupiter.api.Assertions.")
    code = code.replace("import org.junit.Assert;", "import org.junit.jupiter.api.Assertions;")

    return code


def run_maven_test(test_class_name):
    print(f"   [Build] Compiling and running test via Maven...")
    cmd = ["mvn", "test", f"-Dtest={test_class_name}", "-Drat.skip=true"]
    try:
        process = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, timeout=60)
        if process.returncode == 0:
            return "PASS", "Build Successful"
        else:
            error_output = process.stdout + "\n" + process.stderr
            lines = error_output.split('\n')
            # Extract actual compiler errors highlighting exactly what failed
            compiler_errors = [line for line in lines if "[ERROR]" in line and ".java:" in line]

            summary = "\n".join(compiler_errors[:5]) if compiler_errors else "Compilation/Execution Failure"
            return "FAIL", summary
    except subprocess.TimeoutExpired:
        return "FAIL", "Timeout during execution"


# --- Main Execution Loop ---
print("Starting Automated Generation & Evaluation Pipeline...")
print(f"Artifacts will be saved to: {output_dir}")

for class_name, meta in TARGET_CLASSES.items():
    if not os.path.exists(meta['source']):
        print(f"Error: Target class path {meta['source']} not found.")
        continue

    with open(meta['source'], "r", encoding="utf-8") as f:
        raw_source = f.read()

    for model in MODELS:
        for strategy in ["Zero-Shot", "Template-Driven"]:
            print(f"\n==============================================")
            print(f"[Running] Model: {model} | Class: {class_name} | Strategy: {strategy}")

            cleanup_old_tests()

            full_prompt = PROMPTS[strategy].replace("{SOURCE_CODE}", raw_source).replace("{CLASS_NAME}", class_name).replace("{PACKAGE_NAME}", meta['package'])
            current_prompt = full_prompt

            max_retries = 3 if strategy == "Template-Driven" else 0
            iteration = 0
            status = "FAIL"
            logs = "Initial State"
            expected_class = f"LlmGenerated{class_name}Test"

            while iteration <= max_retries:
                attempt_label = "Initial Attempt" if iteration == 0 else f"Retry {iteration}/{max_retries}"
                print(f" -> {attempt_label}")

                try:
                    response = ollama.generate(model=model, prompt=current_prompt)
                    java_code = extract_java_code(response['response'], expected_class)

                    if java_code == "HALLUCINATION_REJECTED":
                        print("   [Result] Status: FAIL (AI hallucination/formatting error)")
                        status = "FAIL"
                        logs = "AI Hallucination: Generated text instead of code."
                        break

                    os.makedirs(os.path.dirname(meta['test_path']), exist_ok=True)
                    with open(meta['test_path'], "w", encoding="utf-8") as test_file:
                        test_file.write(java_code)

                    status, logs = run_maven_test(expected_class)
                    print(f"   [Result] Status: {status}")

                    if status == "PASS":
                        break
                    else:
                        current_prompt = PROMPTS["Iterative"].replace("{ERROR_LOG}", logs)
                        iteration += 1

                except Exception as e:
                    print(f"   [Error] Process crash: {str(e)}")
                    status = "CRASH"
                    logs = str(e)
                    break

            # --- Save the Artifact to Desktop ---
            if os.path.exists(meta['test_path']):
                safe_strategy = strategy.replace(" ", "_").replace("-", "_")
                dest_filename = f"{class_name}_{safe_strategy}_Final.java"
                shutil.copy(meta['test_path'], os.path.join(output_dir, dest_filename))

            results.append({
                "Model": model,
                "Target Class": class_name,
                "Prompt Strategy": strategy,
                "Iterations Needed": iteration if status == "PASS" else "Failed after max retries",
                "Final Status": status,
                "Execution Logs/Errors": logs
            })

# --- Finalize CSV ---
csv_path = os.path.join(output_dir, "results.csv")
df = pd.DataFrame(results)
df.to_csv(csv_path, index=False)

print("\n==============================================")
print(f"Experiment Complete! Results and Java files saved to:\n{output_dir}")
print("==============================================")
