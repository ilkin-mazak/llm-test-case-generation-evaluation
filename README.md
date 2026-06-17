# LLM Test Case Generation and Evaluation Pipeline

This repository contains the prototype orchestrator script and experimental pipeline for generating, evaluating, and iteratively refining software unit tests using Large Language Models (LLMs). 

The system shifts away from single-pass AI code generation and implements an automated, multi-gated feedback loop. It enforces technical validity, structural coverage, and semantic fault detection by feeding pipeline errors directly back to the LLM for self-correction.

---

## Target Software System
The pipeline is designed and tested against **Apache Commons Lang (v3.x)**. This Java repository was selected due to its strictly structured, isolated utility classes spanning low, medium, and high complexity constraints (e.g., `StringUtils.java`, `DateUtils.java`).

* **Target Source Code (Public Repository):** [apache/commons-lang](https://github.com/apache/commons-lang)

---

## Core Pipeline Architecture
The workflow relies on a custom Python orchestrator that handles REST API connections, subprocess execution, and file I/O. The generated Java test suites must pass three strict evaluation gates:

1. **Gate 1 (Technical Validity):** Automated `mvn test` builds to verify syntax, imports, and execution.
2. **Gate 2 (Structural Adequacy):** JaCoCo branch and line coverage analysis against predefined thresholds.
3. **Gate 3 (Semantic Validation):** PIT Mutation testing to measure independent oracle strength and verify true fault-detection capabilities.

---

## Repository File Structure

```text
├── run_experiment.py     # Main Python orchestrator script managing API requests, subprocesses, and file I/O.
├── /prompts              # Contains logic for Zero-Shot, Template-Driven Sandboxing, and Iterative Refinement prompts.
├── /target_project       # Cloned directory where the Apache Commons Lang source code is evaluated.
├── /results              # Output directory for pipeline metrics, evaluation logs, and extracted JaCoCo/PIT reports.
└── README.md             # Project documentation.
