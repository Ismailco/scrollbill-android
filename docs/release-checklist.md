# ScrollBill Beta Release Checklist

## Repository and application

- [x] Application ID checked: `com.soultware.scrollbill`
- [x] Version checked: `versionCode 1`, `versionName 0.1.0`
- [x] Launcher, adaptive, and monochrome icon resources included
- [ ] Privacy policy draft reviewed and hosted publicly
- [ ] Google Play developer account and app record prepared
- [ ] Upload signing key configured securely outside Git

## Build and privacy review

- [ ] Release AAB generated and signed with the intended upload key
- [x] Release APK/AAB build commands verified locally
- [ ] Release build smoke-tested on a physical device
- [x] Usage Access flow reviewed
- [x] Scoped launcher package-visibility declaration reviewed
- [x] No Internet, broad package visibility, storage, notification, location, or contacts permissions
- [ ] Google Play Data Safety form reviewed against current policy
- [ ] Content rating completed

## Store preparation

- [ ] Screenshots created from controlled data
- [ ] Feature graphic prepared if required by Play
- [ ] Privacy-policy URL provided
- [ ] Store listing completed and reviewed
- [ ] Internal or closed testing track configured
- [ ] Test installation from Google Play completed
- [ ] Upgrade path tested with a later, higher `versionCode`

Items marked incomplete require a release owner or Google Play Console and are not claimed as complete by this repository.
