# ScrollBill Privacy Policy (Draft)

**Effective date:** [To be set before publication]

This draft describes ScrollBill version 0.1.0 as currently implemented. It is intended for review before being published at a public privacy-policy URL.

## What ScrollBill accesses

ScrollBill uses Android Usage Access to read application foreground-usage history for the last seven completed local calendar days. It uses application names and icons, when Android makes that metadata available, to present the report. Android Usage Access is granted or revoked by the user in Android system settings.

The app declares `PACKAGE_USAGE_STATS` and a narrow launcher-intent package-visibility query for labels and icons. It does not request contacts, location, storage, notification access, or accessibility access.

## Local processing and retention

Reports and calculations are performed on the device. ScrollBill has no account, backend, cloud database, network client, analytics, advertising, or crash-reporting service. It does not sell personal data.

The app does not persist a usage database, report history, or installed-app inventory. A generated receipt PNG is written temporarily to the app's private cache so Android sharing targets can read it. The current receipt file is overwritten on a later share and is not saved to public storage.

## Sharing

Sharing happens only after the user taps **Share receipt** and chooses a target in Android's Sharesheet. ScrollBill supplies the generated image through a temporary `content://` URI using its non-exported FileProvider. ScrollBill does not automatically upload or publish the receipt. The receiving app's handling of the image is outside ScrollBill's control.

## What ScrollBill does not collect

ScrollBill does not collect or request an account identifier, name, email address, location, contacts, advertising identifier, payment information, or intentionally collected health information. It does not provide medical, addiction-treatment, parental-control, or app-blocking functionality.

## Contact

Privacy questions: [Project contact to be provided before publication]

This draft does not make a legal-compliance certification or claim a particular jurisdictional status.
