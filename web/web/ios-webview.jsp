<%@ page language="java" pageEncoding="UTF-8"%>
<script type="text/javascript" src="js/WebViewJavascriptBridge.js"></script>
<script type="text/javascript">
function connectWebViewJavascriptBridge(callback) {
	if (window.WebViewJavascriptBridge) {
		callback(WebViewJavascriptBridge)
	} else {
		document.addEventListener('WebViewJavascriptBridgeReady', function() {
			callback(WebViewJavascriptBridge)
		}, false)
	}
}

connectWebViewJavascriptBridge(function(bridge) {

	bridge.init(function(message, responseCallback) {
		var data = { 'Javascript Responds':'init!' }
		responseCallback(data)
	})

	bridge.registerHandler('jumpError', function(data, responseCallback) {
		var responseData = { 'Javascript Says':'registerHandler!' }
		responseCallback(responseData)
	})
	
	bridge.send('<%=errorStr.isEmpty() ? "数据错误" : errorStr%>', function(responseData) {
	})
})
</script>