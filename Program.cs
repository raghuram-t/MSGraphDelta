// See https://aka.ms/new-console-template for more information
using Azure.Identity;
using Microsoft.Graph;
using Microsoft.Kiota.Abstractions;
using Microsoft.Graph.Users.Delta;
using Microsoft.Graph.Models;
using Azure.Core;

Console.WriteLine("Hello, World!");
var scopes = new[] { "User.Read" };

// Multi-tenant apps can use "common",
// single-tenant apps must use the tenant ID from the Azure portal
var tenantId = "de29a90e-98e1-4ce0-a3b8-f9f38f3fd6d6";

// Value from app registration
var clientId = "6dffb3e6-85e6-49b4-b663-bf896d46c81f";

// using Azure.Identity;
var options = new UsernamePasswordCredentialOptions
{
    AuthorityHost = AzureAuthorityHosts.AzurePublicCloud,
};

var userName = "program@rrorbit.in";
var password = "password*900";

// https://learn.microsoft.com/dotnet/api/azure.identity.usernamepasswordcredential
var userNamePasswordCredential = new UsernamePasswordCredential(
    userName, password, tenantId, clientId, options);

var graphClient = new GraphServiceClient(userNamePasswordCredential, scopes);

// Get the Initial FullState

var result = await graphClient.Users.Delta.GetAsDeltaGetResponseAsync(requestConfiguration => {
    requestConfiguration.QueryParameters.Select = new string[] { "userPrincipalName", "displayName" };
});

foreach (var user in result.Value)
{
    Console.WriteLine(user.DisplayName + " :: " + user.UserPrincipalName + " :: " + user.Id);
}

Console.WriteLine("wait");

var nextLink = result.OdataNextLink;
var deltaLink = result.OdataDeltaLink;

int i = 0;


while (nextLink != null) {

    //Console.WriteLine("nextLink Value: " + nextLink);
    var deltaRequest = new DeltaRequestBuilder(nextLink, graphClient.RequestAdapter);
    Console.WriteLine("Initiating HTTP Delta Graph call");
    var deltaResult = await deltaRequest.GetAsDeltaGetResponseAsync();
    nextLink = deltaResult.OdataNextLink;

    foreach (var user in deltaResult.Value)
    {
        Console.WriteLine(user.DisplayName + " :: " + user.UserPrincipalName + " :: " + user.Id);

    }

    if (nextLink == null) {

        deltaLink = deltaResult.OdataDeltaLink;
        Console.WriteLine("No data to receive at the moment, will try after 20 seconds with Delta Link");
        Thread.Sleep(20000);
        nextLink = deltaLink;
        i++;
        Console.WriteLine("Delta Iteration # " + i);

    }
 
}











Console.WriteLine("break");


/*
 * 
 * var result = await graphClient.Users.GetAsync();

/* Get ALL the users with select parameters
var result = await graphClient.Users.GetAsync(requestConfiguration =>
{
    requestConfiguration.QueryParameters.Select =
        new string[] { "displayName", "jobTitle" };
});


Console.WriteLine("Got {0} users", result.Value.Count);

foreach (var user in result.Value) {

    Console.WriteLine(user.DisplayName);
}
*/
