# Presently: A Gratitude Journal
[![CircleCI](https://circleci.com/gh/alisonthemonster/Presently/tree/develop.svg?style=svg)](https://circleci.com/gh/alisonthemonster/Presently/tree/develop)

A journal to write what you're thankful for. Contributions welcome!

[Google Play Posting](https://play.google.com/store/apps/details?id=journal.gratitude.com.gratitudejournal&hl=en)

### Philosophy 
Presently is built on the idea that gratitude journaling should be free, private, and available to all. It will never have ads, never have a premium version, and will always be open source. Entries are kept on device and can be exported to CSV.

Presently is also built with the best Android practices in mind. It follows MVVM to separate concerns and ensure we write testable code. We require 80%+ code coverage and rely on both unit tests and instrumented tests (with a heavier weight towards unit tests). Instrumented tests are run on Firebase Test Lab, crashes are reported through Crashlytics, and analytics are reported with Firebase Analytics.

We support several languages currently and are always looking for translators to help us! We support French, Spanish, Italian, German, Finnish, Arabic, Dutch, Polish, Portuguese, Slovakian, Croatian, Romanian, Turkish, Russian, Afrikaans.

### Tech Stack [So far...]
- MVVM with architecture components
   - ViewModel
   - Room (with FTS and Paging v3)
- Mavericks
- CircleCI
- Jacoco coverage reports (integrated with CI)
- Firebase Crashlytics Crash Reporting
- Dagger + Hilt
- Espresso
- Firebase Test Lab
- WorkManager
- Dropbox Java SDK

## Design
<img src="https://i.imgur.com/EN4oOF4.png" width="200"> <img src="https://i.imgur.com/8RJvH6E.png" width="200"> <img src="https://i.imgur.com/UQQ7wbq.png" width="200"> <img src="https://i.imgur.com/JtYuyq2.png" width="200">


The app currently has over 40 themes to choose from! Here's a few:

<img src="https://user-images.githubusercontent.com/10744793/189284037-5d19021b-17c4-4229-a769-bb8ee74fd8d6.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/189284043-4ddf2749-e0b6-456a-b6fa-35f49157c3f5.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/189284046-bf74da64-02e7-45a9-a77d-b357ca300bf8.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/189284047-d70bde92-8c66-4dea-85ef-bcfd5165dd66.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/189284049-30e28cf9-5751-4de8-bf29-96ff4c274ef6.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/189284050-082145de-e35e-4d80-a2bf-ed27d80d1b13.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/137650501-69ed8e1b-0589-4ae8-81e0-cd8bbb9a779c.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/136122717-de9dc39e-7a85-4b6a-87f7-33d13b72340d.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/136122723-94abd81a-223d-40d3-8f17-d60b8ac936a3.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/136122868-fc770228-5185-4aef-b8d6-c7f02fce822f.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/137650505-d26bad6e-02aa-4929-9672-06451e38322d.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/137650495-fb0c5fdf-d38e-48c5-bd8c-2c660e6196e3.png" width="200"/>


### Contributions
Contributions are welcome! Please fork the repo and make a PR. Forked PRs will build the app and run unit tests but will not run instrumented tests. A repo owner will run those tests as they require secret keys. To make the app build locally you'll need to add a Dropbox Key (or any string if you don't want to test Dropbox backup features) to the `local.properties` file. Like so: `DROPBOX_KEY=myDropboxKey`.


### Dependency Graph
Presently is a small and simple app, did it need modularizing? Probably not. But Presently also servees as a playground for me for trying out new technologies and keeping up with best practices. Here's how we've broken down the app so far.
![](dependency-graph/project.dot.png)
