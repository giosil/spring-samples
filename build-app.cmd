@echo off

cd .\src\main\resources\static\ts

echo Compile App...
call tsc --declaration --project ./app/tsconfig.json

rem Install first https://www.npmjs.com/package/uglify-js
rem npm install uglify-js -g

echo Minify...
call uglifyjs -c -o ../app/app.min.js -m -- ../app/app.js

rem Refresh Spring-Boot
IF EXIST ..\..\..\..\..\target (
	copy ..\app\*.* ..\..\..\..\..\target\classes\static\app
	echo. >> ..\..\..\..\..\target\classes\application.properties
)
