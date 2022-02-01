# Presently: A Gratitude Journal
[![CircleCI](https://circleci.com/gh/alisonthemonster/Presently/tree/develop.svg?style=svg)](https://circleci.com/gh/alisonthemonster/Presently/tree/develop)

A journal to write what you're thankful for. Contributions welcome!

[Google Play Posting](https://play.google.com/store/apps/details?id=journal.gratitude.com.gratitudejournal&hl=en)

### Philosophy 
Presently is built on the idea that gratitude journaling should be free, private, and available to all. It will never have ads, never have a premium version, and will always be open source. Entries are kept on device and can be exported to CSV.

Presently is also built with the best Android practices in mind. It follows MVVM to separate concerns and ensure we write testable code. We require 80%+ code coverage and rely on both unit tests and instrumented tests (with a heavier weight towards unit tests). Instrumented tests are run on Firebase Test Lab, crashes are reported through Crashlytics, and analytics are reported with Firebase Analytics.

We support several languages currently and are always looking for translators to help us! We support French, Spanish, Italian, German, Finnish, Arabic, Dutch, Polish, Portuguese, Slovakian, Croatian, Romanian, Turkish, Russian, & Afrikaans.

### Tech Stack [So far...]
- MVVM and MVI (Mavericks)
- Room (with FTS and Paging v3)
- CircleCI
- Jacoco coverage reports (integrated with CI)
- Firebase Crashlytics Crash Reporting
- Dagger + Hilt
- Espresso
- Firebase Test Lab
- WorkManager
- Dropbox Java SDK

<img src="https://i.imgur.com/EN4oOF4.png" width="220"> <img src="https://i.imgur.com/8RJvH6E.png" width="220"> <img src="https://i.imgur.com/UQQ7wbq.png" width="220"> <img src="https://i.imgur.com/JtYuyq2.png" width="220">

### Contributions
Contributions are welcome! Please fork the repo and make a PR. Forked PRs will build the app and run unit tests but will not run instrumented tests. A repo owner will run those tests as they require secret keys. 

Presently Android has a GNU General Public License. This means that you have the right to use, modify, and distrubute our code. GPL is a Copyleft License, that means that while we allow modifications, if they are released they must also be licensed under GPL. Additionally since we're hosted on Github, this code follows Github's Terms of Service. What that means for contributions is that when you submit a pull request you are agreeing to contribute under the same license as the repo (see [Contributions Under Repository License](https://docs.github.com/en/github/site-policy/github-terms-of-service#6-contributions-under-repository-license)). 
If you want your contributions to reach our thousand's of daily users then you'll want to submit your contributions here! To learn more, we like [this description of Copyright and Licensing](https://ben.balter.com/2015/06/03/copyright-notices-for-websites-and-open-source-projects/#the-copyright-holder) from Ben Balter.

### Building Presently Locally
To make the app build locally you'll need to add a Dropbox Key (or any string if you don't want to test Dropbox backup features) to the `local.properties` file. Like so: `DROPBOX_KEY=myDropboxKey`.
Note that we do not include the real font packages for Presently, so your app will not exactly look like production. The real fonts are added in our manual release process by project owners.

### Dependency Graph
![](dependency-graph/project.dot.png)


Copyright 2018 - 2022, Alison Wyllie and the Presently contributors.
