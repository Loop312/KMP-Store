plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktor) apply false
}
val buildOutputsDir = layout.projectDirectory.dir("!buildOutputs")

tasks.register("collectBuilds") {
    group = "distribution"
    description = "Collects build artifacts from all subprojects into the root buildOutputs folder."

    notCompatibleWithConfigurationCache("This task accesses the mutable subprojects tree to aggregate outputs.")

    // 1. Forces all subprojects to run 'assemble' before this task starts
    dependsOn(subprojects.map { it.tasks.matching { task -> task.name == "assemble" } })
    dependsOn(subprojects.map { it.tasks.matching { task -> task.name == "createDistributable" } })
    doLast {
        // Clean the output directory before copying to avoid stale builds
        if (buildOutputsDir.asFile.exists()) {
            buildOutputsDir.asFile.deleteRecursively()
        }
        buildOutputsDir.asFile.mkdirs()

        subprojects {
            val subprojectName = this.name
            val subprojectBuildDir = this.layout.buildDirectory.asFile.get()

            // Track if we found ANY artifacts for this subproject
            var foundAnyArtifact = false

            // Helper function to safely copy with error handling and warnings
            fun copyArtifacts(dirName: String, sourceDir: File, targetDir: Directory, includePattern: String? = null) {
                if (sourceDir.exists()) {
                    foundAnyArtifact = true
                    try {
                        copy {
                            from(sourceDir)
                            into(targetDir)
                            if (includePattern != null) {
                                include(includePattern)
                            }
                        }
                    } catch (e: Exception) {
                        logger.warn("❌ Failed to copy $dirName for subproject '$subprojectName': ${e.message}")
                    }
                }
            }

            // 1. Android Outputs
            copyArtifacts(
                dirName = "Android APKs",
                sourceDir = File(subprojectBuildDir, "outputs/apk"),
                targetDir = buildOutputsDir.dir("$subprojectName/android"),
                includePattern = "**/*.apk"
            )

            // 2. JS Outputs
            copyArtifacts(
                dirName = "JS Production Executable",
                sourceDir = File(subprojectBuildDir, "dist/js/productionExecutable"),
                targetDir = buildOutputsDir.dir("$subprojectName/js")
            )

            // 3. Wasm JS Outputs
            copyArtifacts(
                dirName = "Wasm JS Production Executable",
                sourceDir = File(subprojectBuildDir, "dist/wasmJs/productionExecutable"),
                targetDir = buildOutputsDir.dir("$subprojectName/wasm")
            )

            // 4. JVM Desktop Outputs
            copyArtifacts(
                dirName = "Desktop Binaries",
                sourceDir = File(subprojectBuildDir, "compose/binaries/main/app"),
                targetDir = buildOutputsDir.dir("$subprojectName/desktop")
            )

            // Warning fallback if a subproject yielded absolutely nothing
            if (!foundAnyArtifact) {
                logger.lifecycle("⚠️ Note: No build outputs found for subproject '$subprojectName'. Skipped.")
            }
        }
    }
}