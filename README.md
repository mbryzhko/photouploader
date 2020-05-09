# Photo Uploader
Very simple command line tool for automating photo archiving.
  
Having your images on your local computer grouped by date, this tool allows uploading files by mask 
to Google Photos. A new album is created automatically. Name of the album is parsed from folder name that should match a date.   
For instance:
- /photo/**2020-01-02**/
- /photo/**2020_01_02**/
- /photo/**2020_01_02-vacation**/jpeg
- /photo/**2020_01_02 family**/

## How to install
1. Checkout this repo: `git clone https://github.com/mbryzhko/photouploader.git`
2. Compile: `mvnw install -DskipTests`
3. Make a copy of configuration file: `/bin/pu.yaml.origin` -> `/bin/pu.yaml`
4. Specify credentials. See `How to retrieve Google Photos Access Key`.
5. Add absolute path of`/bin/pu[.cmd]` executable file into `Path`.

## How to retrieve Google Photos Access Key
1. Open [Google OAuth Playground](https://developers.google.com/oauthplayground/) in a browser.
2. Step #1. Specify scope: `https://www.googleapis.com/auth/photoslibrary, https://www.googleapis.com/auth/photoslibrary.sharing` and press `Authorize APIs`.
3. On the next screen select your Google Photos Account.
4. Step #2. Press button to exchange auth code for the Access Token.
5. Copy value of the Access token.

## How to run
1. Navigate to a directory with images that should be uploaded.
2. Run CLI command: `pu upload`