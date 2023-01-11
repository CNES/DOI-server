<html>
<head>
<title>DOI status page</title>
</head>
<body style="font-family: serif;">
<div class="container">
    <div class="header">
        <div class="application">
            ${applicationName}
        </div>   
    </div>    
    <div class="row">
        <div class="col-md-12">
            <div class="error-template">
                <h1>An error occured!</h1>
                <h1>${statusCode} - ${statusName}</h2>
                <div id="error" class="error-details">
                    Sorry, an error has occured, ${statusDescription}!
                </div>
                <div class="error-actions">                    
                    <a href="/mds?method=OPTIONS" class="btn btn-primary btn-lg"><span class="glyphicon glyphicon-home"></span>
                        Take Me Documentation </a><a href="mailto:${contactAdmin}" class="btn btn-default btn-lg"><span class="glyphicon glyphicon-envelope"></span> Support</a>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
