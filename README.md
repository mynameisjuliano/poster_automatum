# poster_automatum
This is a program in which I use to generate post content from our business' supplier's
posts to content on which we can post to our page. It generates the capital and other
details. Though, sometimes manual intervention is needed for a complete post. I'm planning
to rewrite this in C, because I think its just more portable is easy that way. (a proof of
this is that I can rewrite, compile and run this program \[if it's in C\] in my phone via
Termux and I can get my job done easily!).

run ``java -jar Poster_Automatum_II-SNAPSHOT-1.0.jar -h`` for more info.

Basically, you can run it with arguments -p -t -i (for page_id \[the page to post to\], access_token
\[user access token, it will be converted to page access token, of the admin of the page\], and input
file \[the json file on which to parse the data to generate, see -J for more info\]) or you can run it
with -m to the location of a file on which the data for this args are supplemented (except -i), the json
should contain the string ``"access_token"`` and ``"page_id,"``for user access token and page ID, respectively
can also contain ``"app_secret,"`` ``"app_id,"`` ``"disable_post,"`` and ``"disable_image" which is the
app's secret code, app ID, (both found in graph API tool) the switch for disabling the posts, and disabling the
image generation of posts (for my day stories). You could also use -r instead of -i for inputting a raw json string
of which should contain the contents of -i. I use this when I mess up on an old data and needed to fix only one. So
instead of running the program for the whole json, I just input a single or few entries in an array to fix the particular
issue.

Also! Use JetBrain's Runtime Environment instead of Oracle or OpenJDK. It has support for colored emojies in Graphics2D, and
also supports JPEG, which saves alot of space. I'm using:
```shell
openjdk version "17.0.6" 2023-01-17
OpenJDK Runtime Environment JBR-17.0.6+10-829.5-jcef (build 17.0.6+10-b829.5)
OpenJDK 64-Bit Server VM *JBR-17.0.6+10-829.5-jcef* (build 17.0.6+10-b829.5, mixed mode)
```

(see that it uses **JBR-17.0.6...**? It works for me and I only found this on an accident. Almost gave up on this program because
monochrome emojies aren't attractive enough)

I hope I can use the C version already, I feel like its much slimmer, hehe. Contribute if you want, and also I hope
you can find this useful somehow, haha.
