set resourcesPath=%userprofile%\lib\resources\java-mutation-framework
xcopy /S /Y /I .\resources\lib %resourcesPath%\lib
xcopy /S /Y .\resources\microbatConfig.json %resourcesPath%
