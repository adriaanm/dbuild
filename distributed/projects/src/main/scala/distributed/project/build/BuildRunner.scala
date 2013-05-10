package distributed
package project
package build

import java.io.File
import model._

/** Runs a build in the given directory. */
trait BuildRunner {
  /**
   * Runs the build for a project in the given directory with a set of incoming
   * repository information.
   * 
   * @param b The project to build
   * @param dir The directory in which to run the build (should be already resolved)
   * @param input Information about artifact read/write repositories and versions.
   * @param log  The log to write to.
   */
  def runBuild(b: RepeatableProjectBuild, dir: java.io.File, input: BuildInput, log: logging.Logger): BuildArtifacts
  
  /**
   * Expands the build options (the 'extra' field) so that the defaults
   * that apply for this build system are taken into account.
   * 
   * @param proj The project configuration that should be expanded
   */
  def expandExtraDefaults(proj: ProjectBuildConfig): ProjectBuildConfig
  
  /**
   * Determines the appropriate base for a project-specific ".dbuild" directory
   * for a project in "dir", configured according to "config".
   * 
   * @param dir The dir containing the checkout of the project
   * @param config The configuration record of this project
   */
  def projectDbuildDir(dir: File, proj: ProjectBuildConfig): File
}

/** Aggregate builder. */
class AggregateBuildRunner(systems: Seq[BuildSystem]) extends BuildRunner {
  private def findBuildSystem(proj: distributed.project.model.ProjectBuildConfig): distributed.project.BuildSystem = {
    systems find (_.name == proj.system) getOrElse sys.error("Could not find build system for " + proj.system)
  }

  def runBuild(b: RepeatableProjectBuild, dir: java.io.File, input: BuildInput, log: logging.Logger): BuildArtifacts =
    findBuildSystem(b.config).runBuild(b, dir, input, log)

  def expandExtraDefaults(proj: ProjectBuildConfig): ProjectBuildConfig =
    findBuildSystem(proj).expandDefaults(proj)
  
  def projectDbuildDir(dir:File, proj: ProjectBuildConfig): File =
    findBuildSystem(proj).projectDbuildDir(dir, proj)
}