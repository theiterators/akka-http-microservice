FROM gitpod/workspace-full

RUN echo 'unset JAVA_TOOL_OPTIONS' >> /home/gitpod/.bashrc.d/99-clear-java-tool-options && rm -rf /home/gitpod/.sdkman

RUN curl -fLo cs https://git.io/coursier-cli-linux &&\
    chmod +x cs &&\
    ./cs java --jvm adopt:1.8.0-292 --env >> /home/gitpod/.bashrc.d/90-cs &&\
    ./cs install --env >> /home/gitpod/.bashrc.d/90-cs &&\
    ./cs install \
      bloop \
      sbt-launcher \
      scala:2.13.6 &&\
    ./cs fetch org.scala-sbt:sbt:1.5.5 >/dev/null &&\
    rm -f cs
