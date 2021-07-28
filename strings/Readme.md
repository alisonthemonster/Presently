# Strings Module
This module contains all of the string resources used in Presently. Since Presently is translated by volunteers, we have moved all the string resources into one place to allow our translators to easily find all the copy they need to translate. 

## How to translate Presently in Github
1. Fork this repository
2. In your forked repository create a new directory in `Presently/strings/src/main/res/`. The name of the directory should be `values-XX` where `XX` is your language's locale code. You can find the locale codes [here](http://www.iana.org/assignments/language-subtag-registry/language-subtag-registry). Note there are special cases if you are doing a translation variant like Brazilian Portuguese. 
3. Inside your new values directory create a `strings.xml` file and copy all of the contents of this [file](https://github.com/alisonthemonster/Presently/blob/develop/strings/src/main/res/values/strings.xml) into it.
4. Translate all of the strings that are marked that don't have `translatable="false"` in them. Follow the instructions below to see how to translate string resources in XML. Make sure to commit your changes.
5. Create another file called `prompts.xml` in your directory and copy all of the contents of this [file](https://github.com/alisonthemonster/Presently/blob/develop/strings/src/main/res/values/prompts.xml). 
4. Translate all of the strings in this file. Follow the instructions below to see how to translate string resources in XML. Make sure to commit your changes.
5. Open the file [StoreListing.md](https://github.com/alisonthemonster/Presently/blob/develop/StoreListing.md) and at the end of the file add the following but translated for your language. (Please note that there are some character length limitations for some of these translations. You do not need to copy the notes to the translators.)  When complete, commit the file.
```
[NOTE TO TRANSLATORS: please keep the following line under 50 characters long]

Presently: A gratitude journal

[NOTE TO TRANSLATORS: please keep the following line under 80 characters long]

Celebrate your daily life with Presently, a free & private gratitude journal

[NOTE TO TRANSLATORS: no length limits below this point, translate however you like!]

Practice gratitude with this simple, always free, and private gratitude journaling app.

Presently lets you: ⁕ Record daily entries of gratitude ⁕ Reflect back on your past moments of gratitude ⁕ Find motivation through quotes and prompts ⁕ Set daily reminders to continue your gratitude practice ⁕ Lock your entries with fingerprint or face id ⁕ Automatically back up your entries to Dropbox ⁕ Search your old entries ⁕ Share your entries with family and friends ⁕ Export/import your entries ⁕ Switch to your favorite color scheme

Presently is 100% free and ad free. All of your entries stay on your device and in your control.
```

8. *OPTIONAL*: Create another file called `inspirations.xml` in your directory and copy all of the contents of this [file](https://github.com/alisonthemonster/Presently/blob/develop/strings/src/main/res/values/inspirations.xml). This is optional because these are the app's gratitude quotes and quotes do not always translate well and are very time consuming. They are not a requirement to translate, but are very much appreciated by our users!
9. When you are happy with your translation, follow [these instructions](https://docs.github.com/en/github/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request-from-a-fork) to create a Pull Request of your translation. A pull request is how code is reviewed and merged into the main codebase. When a Pull Request is created several tests are automatically run to ensure that the app still works as intended. Please add a short description of your translation. 
10. Respond to any comments and make the necessary changes. If your translation is approved, an owner of the Presently repo will merge your pull request and your translation will go out in the next release!

## How to translate Presently without creating a Github Pull Request
If you don't know how to use Github you can still translate Presently the old fashioned way! Please follow these instructions and send your translated files to gratitude.journal.app@gmail.com and we will add the translation to Github for you!
1. Download the [strings.xml file]((https://github.com/alisonthemonster/Presently/blob/develop/strings/src/main/res/values/strings.xml) to your computer and translate the copy following the guide below.
2. Download the [prompts.xml file](https://github.com/alisonthemonster/Presently/blob/develop/strings/src/main/res/values/prompts.xml) to your computer and translate the copy following the guide below on string arrays.
3. Translate the following copy and save it in a file:
```
[NOTE TO TRANSLATORS: please keep the following line under 50 characters long]

Presently: A gratitude journal

[NOTE TO TRANSLATORS: please keep the following line under 80 characters long]

Celebrate your daily life with Presently, a free & private gratitude journal

[NOTE TO TRANSLATORS: no length limits below this point, translate however you like!]

Practice gratitude with this simple, always free, and private gratitude journaling app.

Presently lets you: ⁕ Record daily entries of gratitude ⁕ Reflect back on your past moments of gratitude ⁕ Find motivation through quotes and prompts ⁕ Set daily reminders to continue your gratitude practice ⁕ Lock your entries with fingerprint or face id ⁕ Automatically back up your entries to Dropbox ⁕ Search your old entries ⁕ Share your entries with family and friends ⁕ Export/import your entries ⁕ Switch to your favorite color scheme

Presently is 100% free and ad free. All of your entries stay on your device and in your control.
```
4. Optionally download the [inspirations.xml file](https://github.com/alisonthemonster/Presently/blob/develop/strings/src/main/res/values/inspirations.xml) and translate following the guide on string arrays below. This is optional because these are the app's gratitude quotes and quotes do not always translate well and are very time consuming. They are not a requirement to translate, but are very much appreciated by our users!
5. Email us your translated `strings.xml`, `prompts.xml`, Store Listing file, and optional `inspriations.xml` to gratitude.journal.app@gmail.com

## Translating String Resources in XML
XML files follow this pattern:
`<string name="key_name">Translate me!</string>`

The only piece of this file that you will change is what is inside the <string> tags. Note that there are special characters (like `'`) that need to be handled in XML string files, Google explains how to do so [here](https://developer.android.com/guide/topics/resources/string-resource#escaping_quotes).

## Translating String Array Resources in XML
XML arrays follow this pattern:
```
<resources>
    <string-array name="prompts">
            <item>What is something that made you smile?</item>
            .. more items
    </string-array>
</resources>
```
To translate the content in these files you will only need to copy what lies between the `<item>` and `</item>` tags. 
