<html>
<head>
<title>DOI status page</title>
<link rel="stylesheet" type="text/css" href="/resources/css/style.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
</head>
<body style="font-family: serif;">
<div class="container">
    <div class="header">
        <img src="${logo}" width="100px" height="100px">
        <div class="application">
            ${applicationName}
        </div>   
    </div>    
    <div class="row">
        <div class="col-md-12">
            <div class="error-template">
                <h1>
                    Oops!</h1>
                <h2>
                    ${statusCode} ${statusName}</h2>
                <div class="error-details">
                    Sorry, an error has occured, ${statusDescription}!
                </div>
                <div class="error-actions">                    
                    <a href="/mds?method=options" class="btn btn-primary btn-lg"><span class="glyphicon glyphicon-home"></span>
                        Take Me Documentation </a><a href="mailto:${contactAdmin}" class="btn btn-default btn-lg"><span class="glyphicon glyphicon-envelope"></span> Support</a>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
