@echo off

cd .\src\main\resources\static\ts

echo Clean app folder..
del /Q ..\app\*.*

echo Compile WUX...
call tsc --declaration --project ./wux/tsconfig.json

echo Compile App...
call tsc --declaration --project ./app/tsconfig.json

rem Install first https://www.npmjs.com/package/uglify-js
rem npm install uglify-js -g

echo Minify...
call uglifyjs -c -o ../app/wux.min.js -m -- ../app/wux.js
call uglifyjs -c -o ../app/app.min.js -m -- ../app/app.js

rem Install first https://www.npmjs.com/package/uglifycss
rem npm install uglifycss -g

echo Minimize main.css..
call uglifycss ../css/main.css --output ../css/main.min.css

rem Refresh Spring-Boot
IF EXIST ..\..\..\..\..\target (
	copy ..\app\*.* ..\..\..\..\..\target\classes\static\app
	echo. >> ..\..\..\..\..\target\classes\application.properties
)

