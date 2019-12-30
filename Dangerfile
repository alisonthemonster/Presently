# Sometimes it's a README fix, or something like that - which isn't relevant for
# including in a project's CHANGELOG for example
declared_trivial = github.pr_title.include? "#trivial"

# Make it more obvious that a PR is a work in progress and shouldn't be merged yet
warn("PR is classed as Work in Progress") if github.pr_title.include? "[WIP]"

jacoco.minimum_project_coverage_percentage = 85
jacoco.minimum_class_coverage_percentage = 30
jacoco.files_extension = [".kt", ".java"]
jacoco.report "app/build/reports/jacocoTestReport/jacocoTestReport.xml"
