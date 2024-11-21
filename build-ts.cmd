@echo off

cd .\src\main\resources\static\ts

echo Clean dist folder..
del /Q ..\dist\*.*

echo Compile WUX...
call tsc --declaration --project ./wux/tsconfig.json

echo Compile App...
call tsc --declaration --project ./app/tsconfig.json

rem Install first https://www.npmjs.com/package/uglify-js
rem npm install uglify-js -g

echo Minify...
call uglifyjs -c -o ../dist/wux.min.js -m -- ../dist/wux.js
call uglifyjs -c -o ../dist/app.min.js -m -- ../dist/app.js