# Contributing to Aisleron

Thank you for your interest in contributing to Aisleron. To maintain the quality of the project and ensure legal clarity for all users and contributors, please adhere to the following guidelines.

## License and App Store Exception

By contributing to this project, you agree that your contributions will be licensed under the project's AGPL 3.0 license. Furthermore, you agree to the project's App Store Exception, which grants the maintainers permission to distribute the software through digital storefronts (such as the Apple App Store) despite potential conflicts between storefront terms and the AGPL 3.0.

## Developer Certificate of Origin (DCO)

We use a DCO to certify that contributors have the right to submit their code. You must sign off on every commit.

### How to sign off

Add a `Signed-off-by` line to every commit message. You can automate this by using the `-s` flag in your git command:

`git commit -s -m "Your commit message"`

By adding this line, you certify the statements laid out in the Developer Certificate of Origin version 1.1 (available at https://developercertificate.org/).

## Contribution Requirements

All Pull Requests must meet the following standards before they can be merged:

* **Test Coverage:** Pull Requests must include relevant and adequate test coverage. Untested code will not be accepted.
* **Automated Screenshots:** If your changes impact the visual interface, you must update the automated screenshot runner to capture relevant, updated screenshots. This ensures our visual documentation and regression testing remain accurate.
* **Documentation:** If a change impacts functionality or user interface, you must submit a corresponding Pull Request to the [documentation repository](https://github.com/aisleron/aisleron.docs) to update the relevant guides. Please link the documentation PR within the description of your code PR.
* **Code Quality:** Ensure code adheres to the project's existing style and architecture.
* **Localization and Translations:** All new or modified string values must be fully translated into all supported languages. 
    * The use of AI tools for translation is permitted and encouraged to expedite this process. 
    * Contributors are responsible for ensuring that AI-generated translations are contextually accurate and correctly formatted according to the project's localization files (e.g., preserving placeholders or keys).

## AI Usage Policy

* **Code Generation:** The use of AI tools for code generation is permitted. However, the contributor assumes full responsibility for the submitted code. You must verify that the code is correct, secure, and that you fully understand its implementation.
* **Images and Artifacts:** The use of AI-generated images, icons, or other creative artifacts is strictly prohibited. All assets must be original or provided under a compatible manual license.

## Code of Conduct

All participants are expected to follow the project's Code of Conduct. Please report any unacceptable behavior to the project maintainers.
