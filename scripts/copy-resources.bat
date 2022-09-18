set resourcesPath=%userprofile%\lib\resources\java-mutation-framework
xcopy /S /Y /I .\lib %resourcesPath%\lib
xcopy /S /Y .\microbatConfig.json %resourcesPath%
