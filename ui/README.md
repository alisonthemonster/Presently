# UI Module
This module contains all the UI elements like styles, colors, and drawables.


### Adding a new theme
A new theme can be added by adding your own color resources to `colors.xml` and drawable and then creating a style like this:
```
    <style name="AppTheme.<Your theme name>" parent="AppTheme">
        <item name="timelineIcon">@drawable/<your svg drawable></item>
        <item name="timelineLineColor">@color/<your color></item>
        <item name="backgroundColor">@color/<your color></item>
        <item name="hintTextColor">@color/<your color></item>
        <item name="toolbarColor">@color/<your color></item>
        <item name="toolbarItemColor">@color/<your color></item>
        <item name="statusBarColor">@color/<your color></item>
        <item name="android:windowLightStatusBar"><true/false></item>
        <item name="timelineDateTextColor">@color/<your color></item>
        <item name="timelineContentTextColor">@color/<your color></item>
        <item name="entryHeaderTextColor">@color/<your color></item>
        <item name="entryContentTextColor">@color/<your color></item>
        <item name="highlightColor">@color/<your color></item> 
    </style>
```

To get that theme in the theme picker you'll need to add an element to `ThemeFragment.kt` in the `app` module like so:
```
Theme(
                "<your theme name>",
                ContextCompat.getColor(requireContext(), R.color.<your toolbar color>),
                ContextCompat.getColor(requireContext(), R.color.<your background color>),
                ContextCompat.getColor(requireContext(), R.color.<your toolbar item color>),
                ContextCompat.getColor(requireContext(), R.color.<your timeline content color>),
                R.drawable.<your theme drawable>,
                <true/false> //true if your drawable should not be tinted
            )
```

Finally you need to add your theme to the `ContainerActivity.kt` also in the `app` module. You'll need to add your theme to the function `setAppTheme`:
```
"<your theme name>" -> setTheme(R.style.AppTheme_<your theme name>)
```
