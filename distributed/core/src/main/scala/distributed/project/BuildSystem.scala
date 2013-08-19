package distributed
package project

import project.model._
import logging.Logger
import java.io.File
import sbt.Path._

/** An abstraction representing a "hook" into the builder that understands how
 * to extract dependencies and run builds for a  given type of "build system".
 * 
 * This allows customized build execution if needed.
 */
trait BuildSystem {
  /** The name of the build system used in configuration. */
  def name: String  
  /** Extract build dependencies of a given project that uses this build system.
   *  While extracting, it also xpands the build options (the 'extra' field) so
   *  that the defaults that apply for this build system are taken into account.
   *  
   * @param config   The project configuration.
   * @param dir      A local checkout of the project.
   * @param log      The logger to send output to for this build.
   * 
   * @return The dependencies the local project requires.
   */
  def extractDependencies(config: ExtractionConfig, dir: java.io.File, log: Logger): ExtractedBuildMeta
  /**
   * Runs this build system on a project.
   * 
   * @param project       The build configuration for this project
   * @param dir           The local checkout of the project to run.
   * @param info          The locally hosted dependencies and output repository for a build.
   * @param log           The logger to send output into.
   * 
   * @return The BuildArtifacts generated by this build.
   */
  def runBuild(project: RepeatableProjectBuild, dir: java.io.File, info: BuildInput, log: logging.Logger): BuildArtifactsOut
  
  /**
   * Determines the appropriate base for a per-project ".dbuild" directory
   * for a project in "dir", configured according to "config".
   * Build systems can override the default: dir / .dbuild
   * 
   * @param dir The dir containing the checkout of the project
   * @param config The configuration record of this project
   */
  def projectDbuildDir(dir: File, proj: RepeatableProjectBuild): File = dir / ".dbuild"
}