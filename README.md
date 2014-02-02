Image Processing as a Service
=============================

<b>URI format</b>: http://{server}/{id}/{operations} where <br>
* {id} is an id representing the image<br>
* {operations} is a REST formated list of image processing operations<br>

<b>Examples</b>:

Resize: http://localhost:8081/GI5468752/resize/640,480<br>
Filters: http://localhost:8081/GI5468752/f/blur<br>
Resize + Edge Filter + Snow Effects: http://:8081/GI35468752/resize/640,480/f/edge/f/snow <br>

<b>List of Effects and Operations</b> at: 
<a href='https://github.com/karthik20522/SprayImageProcessing/blob/master/src/main/scala/com/imageprocessing/core/ProcessImageActor.scala#L66'>ProcessImageActor.scala</a>

<b>Extract all metadata</b>
http://{server}/{id}/meta/read
