//
//  ViewController.h
//  TweetFeed
//
//  Created by Dev Floater 17 on 5/24/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ViewController : UIViewController <UITableViewDelegate, UITableViewDataSource>
{
    NSURLConnection         *tweetListFeedConnection;
    NSMutableData           *appListData;
    NSMutableArray          *jsonArray;
    
    UITableView *table;
}
@end
