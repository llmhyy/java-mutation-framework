# Setup (For experiments requiring Defects4J)
The environment variables in an IDE may be different from the terminal.
Add the following to your IDE Run Configurations:
- Path to Defects4J bin dir to `$PATH`
- Path to perl module dirs missing in `@INC` to `$PERL5LIB` 
- Path to Java 8 bin dir to `$PATH`. (Print the paths to different Javas on Ubuntu using `update-java-alternatives --list`)
- Path to Java 8 home dir to `$JAVA_HOME`. (Print the paths to different Javas on Ubuntu using `update-java-alternatives --list`)

Example final environment variables on Intellij
`PERL5LIB=/home/chenghin/perl5/lib/perl5/x86_64-linux-gnu-thread-multi;JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64;PATH=/usr/lib/jvm/java-1.8.0-openjdk-amd64/bin:/home/chenghin/Desktop/repos/defects4j/framework/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games:/snap/bin:/usr/local/cuda/bin:CONSUL_HOME:MONGODB_HOME`
# TODO
Shift experiment out, since it should not be part of the mutation framework