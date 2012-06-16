//
//  ViewController.m
//  TweetFeed
//
//  Created by Dev Floater 17 on 5/24/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "ViewController.h"
#import "Cell.h"
#import "SBJson.h"

static NSString *const TwitterFeedURL =
@"http://search.twitter.com/search.json?q=%23bieber";

@interface ViewController ()

@end

@implementation ViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
        
    // setup the tableview
    table = [[UITableView alloc] initWithFrame:self.view.bounds];
    table.delegate = self;
    table.dataSource = self;
    
    //attach the table view to view object in this view controller
    [self.view addSubview:table];
    
    // establish connection to twitter
    NSURLRequest *urlRequest = [NSURLRequest requestWithURL:[NSURL URLWithString:TwitterFeedURL]];
    tweetListFeedConnection = [[NSURLConnection alloc] initWithRequest:urlRequest delegate:self];
    
    NSAssert(tweetListFeedConnection != nil, @"Failure to create URL connection.");    
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
        return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
    } else {
        return YES;
    }
}

#pragma mark tableviewdatasource
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return 10;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *cellId = @"Cell";
    
    Cell *cell = [tableView dequeueReusableCellWithIdentifier:cellId];
    
    if (cell == nil) {
        cell = [[Cell alloc] init];
//                initWithStyle:UITableViewCellStyleDefault reuseIdentifier:cellId];
    }
    
    NSString *cellString = [NSString stringWithFormat:@"Table cell number:%i", indexPath.row + 1];
    cell.textLabel.text = cellString;
    
    return cell;
}

#pragma mark -
#pragma mark NSURLConnection delegate methods

// -------------------------------------------------------------------------------
//	handleError:error
// -------------------------------------------------------------------------------
- (void)handleError:(NSError *)error
{
    NSString *errorMessage = [error localizedDescription];
    UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"Cannot Show tweets"
														message:errorMessage
													   delegate:nil
											  cancelButtonTitle:@"OK"
											  otherButtonTitles:nil];
    [alertView show];
}

// The following are delegate methods for NSURLConnection. Similar to callback functions, this is how
// the connection object,  which is working in the background, can asynchronously communicate back to
// its delegate on the thread from which it was started - in this case, the main thread.
//

// -------------------------------------------------------------------------------
//	connection:didReceiveResponse:response
// -------------------------------------------------------------------------------

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
    // download latest tweets as a json and parse into dictionary object so that
    // it can be used to populate table cells
    NSString *responseString  = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    SBJsonParser *parser = [[SBJsonParser alloc] init];
    NSError *error = nil;
    NSDictionary *jsonObjects = [parser objectWithString:responseString error:&error];
    jsonArray = [jsonObjects objectForKey:@"results"];
    NSLog(@"%i", jsonArray.count);
}


@end
