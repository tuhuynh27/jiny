package cmd

import (
	"fmt"
	"github.com/spf13/cobra"
	"html/template"
	"os"
)

type Metadata struct {
	MainClassName string
	ArtifactName string
}

var rootCmd = &cobra.Command{
	Use:   "jinycli",
	Short: "JinyFramework CLI",
	Long: `Lightweight, modern, simple Java web framework for rapid development in the API era`,
	Run: func(cmd *cobra.Command, args []string) {
		m := Metadata{MainClassName: "com.tuhuynh.Test", ArtifactName: "test"}

		buildGradleTemplate, _ := template.New("build.gradle").Parse(BuildGradle)
		buildGradleFile, _ := os.Create("build.gradle")
		_ = buildGradleTemplate.Execute(buildGradleFile, m)

		settingsGradleTemplate, _ := template.New("settings.gradle").Parse(SettingsGradle)
		settingsGradleFile, _ := os.Create("settings.gradle")
		_ = settingsGradleTemplate.Execute(settingsGradleFile, m)
	},
}

func Execute() {
	if err := rootCmd.Execute(); err != nil {
		_, _ = fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
}
