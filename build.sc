import mill._, javalib._

object libraries{

  object versions {
    val bytebuddy = "1.14.18"
    val junitJupiter = "5.10.3"
    val errorprone = "2.23.0"
  }
  val junit4 = ivy"junit:junit:4.13.2"
  val junitJupiterApi = ivy"org.junit.jupiter:junit-jupiter-api:${versions.junitJupiter}"
  val junitJupiterParams = ivy"org.junit.jupiter:junit-jupiter-params:${versions.junitJupiter}"
  val junitPlatformLauncher = ivy"org.junit.platform:junit-platform-launcher:1.10.3"
  val junitJupiterEngine = ivy"org.junit.jupiter:junit-jupiter-engine:${versions.junitJupiter}"
  val junitVintageEngine = ivy"org.junit.vintage:junit-vintage-engine:${versions.junitJupiter}"
  val assertj = ivy"org.assertj:assertj-core:3.26.3"
  val hamcrest = ivy"org.hamcrest:hamcrest-core:2.2"
  val opentest4j = ivy"org.opentest4j:opentest4j:1.3.0"

  val bytebuddy = ivy"net.bytebuddy:byte-buddy:${versions.bytebuddy}"
  val bytebuddyagent = ivy"net.bytebuddy:byte-buddy-agent:${versions.bytebuddy}"
  val bytebuddyandroid = ivy"net.bytebuddy:byte-buddy-android:${versions.bytebuddy}"

  val errorprone = ivy"com.google.errorprone:error_prone_core:${versions.errorprone}"
  val errorproneTestApi = ivy"com.google.errorprone:error_prone_test_helpers:${versions.errorprone}"

  val autoservice = ivy"com.google.auto.service:auto-service:1.1.1"

  val objenesis = ivy"org.objenesis:objenesis:3.3"

  val osgi = ivy"org.osgi:osgi.core:8.0.0"
  val equinox = ivy"org.eclipse.platform:org.eclipse.osgi:3.20.0"
  val bndGradle =  "biz.aQute.bnd:biz.aQute.bnd.gradle:6.4.0"

  val groovy = ivy"org.codehaus.groovy:groovy:3.0.22"
}

trait MockitoModule extends MavenModule{
  def testIvyDeps: T[Agg[mill.scalalib.Dep]] = Agg.empty[mill.scalalib.Dep]
  def testFramework = "com.novocode.junit.JUnitFramework"
  object test extends MavenTests{
    def testFramework = MockitoModule.this.testFramework
    def ivyDeps =
      testIvyDeps() ++
      Agg(libraries.hamcrest, libraries.junit4, libraries.bytebuddyagent, ivy"com.github.sbt:junit-interface:0.13.2")
  }
}

object mockito extends RootModule with MockitoModule{
  def compileIvyDeps = Agg(
    libraries.hamcrest, libraries.junit4, libraries.bytebuddyagent,
    libraries.bytebuddy,
    libraries.opentest4j
  )

  def ivyDeps = Agg(
    libraries.objenesis
  )

  def testIvyDeps = Agg(
    libraries.assertj,
    libraries.junitJupiterApi,
    libraries.junitJupiterParams
  )

  def resources = T{
    val subpath = os.SubPath("org/mockito/internal/creation/bytebuddy/inject/")
    os.copy(
      compile().classes.path / subpath / "MockMethodDispatcher.class",
      T.dest / subpath / "MockMethodDispatcher.raw",
      createFolders = true
    )
    super.resources() ++ Seq(PathRef(T.dest))
  }
  object errorprone extends MockitoModule{
    def compileIvyDeps = Agg(libraries.autoservice)

    def moduleDeps = Seq(mockito)
    def ivyDeps = Agg(libraries.errorprone)
    def testIvyDeps = Agg(libraries.errorproneTestApi)

    def testFramework = "com.novocode.junit.JUnitFramework"
    def forkArgs = Seq(
//      "-processorpath", libraries.autoservice,
      "-Xbootclasspath/a:${configurations.errorproneJavac.asPath}",
      "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.type=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
    )

    def javacOptions = Seq(
      "--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"
    )
  }
}

