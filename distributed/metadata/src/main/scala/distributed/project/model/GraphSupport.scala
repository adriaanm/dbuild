package distributed.project.model
import graph._

case class BuildNode(value: ProjectConfigAndExtracted) extends Node[ProjectConfigAndExtracted] {
  def hasProject(dep: ProjectRef): Boolean = {
    def hasArtifact(p: Project): Boolean =
      p.artifacts exists (dep == _)
    value.extracted.projects exists hasArtifact
  }
  override def toString() = "   Project \"" + value.config.name + "\". Contains the subprojects:\n" + (value.extracted.projects map { p =>
    "      " + p.organization + ":" + p.name + "\n" +
      (if (p.dependencies.nonEmpty) "         depends on:\n" + p.dependencies.mkString("            ", "\n            ", "\n") else "")
  }).mkString
}

class BuildGraph(builds: Seq[ProjectConfigAndExtracted]) extends Graph[ProjectConfigAndExtracted, Nothing] {
  private val buildNodes = builds.map(b => new BuildNode(b))(collection.breakOut)
  override val nodes: Set[Nd] = buildNodes.toSet

  private val nodeMap: Map[ProjectConfigAndExtracted, graph.Node[ProjectConfigAndExtracted]] =
    buildNodes.map(b => b.value -> b)(collection.breakOut)
  def nodeFor(build: ProjectConfigAndExtracted): Option[Nd] = nodeMap.get(build)
  def nodeForName(name: String):Nd = nodes find (_.value.config.name == name) getOrElse
    sys.error("Project " + name + " was not found in this configuration file.")

  // Memoized ok?
  def edges(n: Nd): Seq[Ed] = edgeMap get n getOrElse Seq.empty
  private val edgeMap: Map[Nd, Seq[Ed]] =
    buildNodes.map(n => n -> edgesImpl(n))(collection.breakOut)
  private def edgesImpl(n: Nd): Seq[Ed] = (for {
    p <- n.value.extracted.projects
    d <- p.dependencies
    m <- buildNodes
    if (m != n) && (m hasProject d)
  } yield EmptyEdge(n, m)).toSet.toSeq
}
