/*
 See the documentation for more options:
 https://github.com/jenkins-infra/pipeline-library/
*/
buildPlugin(
  forkCount: '1C', // Run a JVM per core in tests
  // we use Docker for containerized tests
  useContainerAgent: false,
  configurations: [
    [platform: 'linux', jdk: 11],
    [platform: 'windows', jdk: 11],
])
