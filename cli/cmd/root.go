package cmd

import (
	"fmt"
	"github.com/manifoldco/promptui"
	"github.com/spf13/cobra"
	"os"
	"strings"
	"text/template"
)

type Metadata struct {
	Feature      int
	ProjectName   string
	SourcePackage string
}

var rootCmd = &cobra.Command{
	Use:   "jinycli",
	Short: "JinyFramework CLI",
	Long: `Lightweight, modern, simple Java web framework for rapid development in the API era`,
	Run: func(cmd *cobra.Command, args []string) {
		Intro()

		featuresPrompt := promptui.Select{
			Label: "Jiny Features",
			Items: []string{"Basic", "JSON support", "JSON and basic middlewares (CORS, JWT) support"},
		}
		projectNamePrompt := promptui.Prompt{
			Label:    "Project Name",
			Default:  "test",
		}
		sourcePackagePrompt := promptui.Prompt{
			Label:    "Source Package",
			Default:  "com.yourcompany",
		}
		featureGot, _, err := featuresPrompt.Run()
		if err != nil {
			fmt.Println(err)
			os.Exit(1)
		}
		fmt.Println(featureGot)
		projectNameGot, err := projectNamePrompt.Run()
		if err != nil {
			fmt.Println(err)
			os.Exit(1)
		}
		sourcePackageGot, err := sourcePackagePrompt.Run()
		if err != nil {
			fmt.Println(err)
			os.Exit(1)
		}

		path, _ := os.Getwd()
		path = path +  string(os.PathSeparator)
		addPath := projectNameGot + string(os.PathSeparator)
		path = path + addPath
		m := Metadata{Feature: featureGot, ProjectName: projectNameGot, SourcePackage: sourcePackageGot}

		// Check if dest dir is exist, if not create it
		if _, err := os.Stat(path); os.IsNotExist(err) {
			_ = os.Mkdir(path, os.ModePerm)
		}

		// Create source package path dir
		sourcePackages := strings.Split(m.SourcePackage, ".")
		sourcePackagesPath := ""
		for i := 0; i < len(sourcePackages); i++ {
			sourcePackagesPath += sourcePackages[i] + string(os.PathSeparator)
		}

		// Create src main
		_ = os.MkdirAll(path+string(os.PathSeparator)+"src"+string(os.PathSeparator)+"main"+string(os.PathSeparator)+"java"+string(os.PathSeparator)+sourcePackagesPath, os.ModePerm)
		_ = os.MkdirAll(path+string(os.PathSeparator)+"src"+string(os.PathSeparator)+"test"+string(os.PathSeparator)+"java"+string(os.PathSeparator)+sourcePackagesPath, os.ModePerm)

		buildGradleTemplate, _ := template.New("build.gradle").Parse(BuildGradleTemplate)
		buildGradleFile, _ := os.Create(path + "build.gradle")
		_ = buildGradleTemplate.Execute(buildGradleFile, m)

		settingsGradleTemplate, _ := template.New("settings.gradle").Parse(SettingsGradleTemplate)
		settingsGradleFile, _ := os.Create(path + "settings.gradle")
		_ = settingsGradleTemplate.Execute(settingsGradleFile, m)

		appGradleTemplate, _ := template.New("App.java").Parse(AppTemplate)
		appGradleFile, _ := os.Create(path+string(os.PathSeparator)+"src"+string(os.PathSeparator)+"main"+string(os.PathSeparator)+"java"+string(os.PathSeparator)+sourcePackagesPath+"App.java")
		_ = appGradleTemplate.Execute(appGradleFile, m)

		appTestGradleTemplate, _ := template.New("AppTest.java").Parse(AppTestTemplate)
		appTestGradleFile, _ := os.Create(path+string(os.PathSeparator)+"src"+string(os.PathSeparator)+"test"+string(os.PathSeparator)+"java"+string(os.PathSeparator)+sourcePackagesPath+"AppTest.java")
		_ = appTestGradleTemplate.Execute(appTestGradleFile, m)

		fmt.Println("Your Jiny project was created at: " + projectNameGot)
		fmt.Println("You can start by running: cd " + projectNameGot + " && gradle run")
	},
}

func Execute() {
	if err := rootCmd.Execute(); err != nil {
		_, _ = fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
}
