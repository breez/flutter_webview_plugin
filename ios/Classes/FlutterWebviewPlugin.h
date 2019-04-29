#import <Flutter/Flutter.h>
#import <WebKit/WebKit.h>

static FlutterMethodChannel *channel;

@interface FlutterWebviewPlugin : NSObject<FlutterPlugin, WKScriptMessageHandler>
@property (nonatomic, retain) UIViewController *viewController;
@property (nonatomic, retain) WKWebView *webview;
@end
