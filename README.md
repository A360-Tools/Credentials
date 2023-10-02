# Credential Utilities Package

This package provides a set of utility functions to enhance the management of credentials in Automation Anywhere. It allows you to generate random passwords with specific rules, retrieve credentials dynamically, list credential attributes, and update credential values. With this package, you can automate the generation and management of credentials, making your bots more robust and secure.

## Building the Project

You can build this project using Gradle with the following command:

```bash
gradle clean build :shadowJar
```

## Problem Solved

This project addresses challenges in credential management within the Automation Anywhere ecosystem:

1. **Generate Secure Passwords**: It enables you to generate psuedo random credential variables following specific ruleset of desired application, ensuring the security of your credentials.

2. **Dynamic Credential Retrieval**: You can retrieve credentials at runtime, allowing you to decide which credential and attribute to use during bot execution, rather than selecting them during development.

3. **List Credential Attributes**: The package provides the ability to list all available credential attributes dynamically, making your bots more versatile.

4. **Update Credential Values**: With this package, you can update credential values, a functionality not possible through standard Automation Anywhere packages.

## Summary

The Credential Utilities Package empowers your bots to:

- Automatically generate new, secure passwords for applications.
- Retrieve credentials from the locker dynamically.
- List available credential attributes on-the-fly.
- Update credential values, enhancing security and flexibility.

| Feature                        | Existing Packages| Credential Utilities Package |
|--------------------------------|----------|-----------------------------|
| Generate Secure Passwords      |   ❌    |   ✔️                        |
| Dynamic Credential Retrieval   |   ❌    |   ✔️                        |
| List Credential Attributes     |   ❌    |   ✔️                        |
| Update Credential Values       |   ❌    |   ✔️                        |
