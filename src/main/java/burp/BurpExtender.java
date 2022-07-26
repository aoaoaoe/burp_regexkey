package burp;


import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BurpExtender extends AbstractTableModel implements IBurpExtender,ITab,IHttpListener,IMessageEditorController{
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    private JSplitPane splitPane;
    private IMessageEditor requestViewer;
    private IMessageEditor responseViewer;
    private IHttpRequestResponse currentlyDisplayedItem;
    private final List<LogEntry> log=new ArrayList<LogEntry>();
    private PrintWriter stdout;



    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        this.callbacks=callbacks;
        helpers=callbacks.getHelpers();
        callbacks.setExtensionName("regexkey");
        stdout = new PrintWriter(callbacks.getStdout(), true);



        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                splitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                Table logTable=new Table(BurpExtender.this);
                JScrollPane scrollPane=new JScrollPane(logTable);
                splitPane.setLeftComponent(scrollPane);

                JTabbedPane tabs=new JTabbedPane();
                //请求和响应数据，传参需要实现IMessageEditorController，重写方法
                requestViewer=callbacks.createMessageEditor(BurpExtender.this,true);
                responseViewer=callbacks.createMessageEditor(BurpExtender.this,false);
                tabs.addTab("request",requestViewer.getComponent());
                tabs.addTab("response",responseViewer.getComponent());
                splitPane.setRightComponent(tabs);

                //在burp上的菜单栏添加一个组件，传参要实现ITabl，需要重写方法
                callbacks.addSuiteTab(BurpExtender.this);

                callbacks.registerHttpListener(BurpExtender.this);
            }
        });
    }

    /**重写IHttpListener的方法**/
    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {

        if (!messageIsRequest){
            synchronized (log){
                int row=log.size();
                if (toolFlag == IBurpExtenderCallbacks.TOOL_REPEATER || toolFlag == IBurpExtenderCallbacks.TOOL_PROXY){
                    IResponseInfo iResponseInfo=helpers.analyzeResponse(messageInfo.getResponse());
                    int bodyoffset=iResponseInfo.getBodyOffset();
                    String body=new String(messageInfo.getResponse()).substring(bodyoffset);
                    stdout.println(body);
                    //文件在target/classes/json下
                    Integer lastIndex=callbacks.getExtensionFilename().lastIndexOf(File.separator);
                    String path = this.callbacks.getExtensionFilename().substring(0, lastIndex) + File.separator;
                    String c = path + "classes\\json\\1.json";

                    JSONParser jsonParser = new JSONParser();
                    Map<String,Object > jsonObject = null;



                    try {
                        jsonObject = (Map) jsonParser.parse(new FileReader(c));
                    } catch (Exception e) {
                        e.printStackTrace();
                        stdout.println(e);
                    }
                    String regex;
                    for (Map.Entry<String,Object > entry:jsonObject.entrySet()){
                        if (entry.getValue().getClass().toString().contains("JSONArray")){
                            JSONArray jsonArray=(JSONArray) entry.getValue();
                            for (Object o : jsonArray) {
                                regex=o.toString();
                                match(regex,body,toolFlag,messageIsRequest,messageInfo,row);
                            }
                        }else {
                            regex=entry.getValue().toString();
                            match(regex,body,toolFlag,messageIsRequest,messageInfo,row);
                        }

                    }

                }

            }
        }

    }

    private void match(String pattern,String body,int toolFlag,boolean messageIsRequest, IHttpRequestResponse messageInfo,int row){
        boolean isMatch= Pattern.matches(pattern,body);
        Matcher matcher=Pattern.compile(pattern).matcher(body);
        List<String> match=new ArrayList<>();
        while (matcher.find()){
            match.add(matcher.group(0));
        }
        if (!match.isEmpty()){
            LogEntry logEntry=new LogEntry(toolFlag,callbacks.saveBuffersToTempFiles(messageInfo),
                    helpers.analyzeRequest(messageInfo).getUrl(),match,pattern);

            if (log.size()!=0){
                int repeat=0;
                for (int i = 0; i < log.size(); i++) {
                    if (log.get(i).url.equals(logEntry.url) && log.get(i).match.equals(logEntry.match)){
                        repeat++;
                    }
                }
                if (repeat==0){
                    log.add(logEntry);
                    fireTableRowsInserted(row,row);
                }
            }else {
                log.add(logEntry);
                fireTableRowsInserted(row,row);
            }
        }
    }

    //重写IMessageEditorController的方法，以下两个同
    @Override
    public IHttpService getHttpService() {
        return currentlyDisplayedItem.getHttpService();
    }
    @Override
    public byte[] getRequest() {
        return currentlyDisplayedItem.getRequest();
    }
    @Override
    public byte[] getResponse() {
        return currentlyDisplayedItem.getResponse();
    }

    //重写AbstractTableModel的方法
    @Override
    public int getRowCount() {
        return log.size();
    }
    @Override
    public int getColumnCount() {
        return 4;
    }
    @Override
    public String getColumnName(int column) {
        switch (column){
            case 0:
                return "Tool";
            case 1:
                return "URL";
            case 2:
                return "match_result";
            case 3:
                return "regex";
            default:
                return "";
        }
    }
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LogEntry logEntry=log.get(rowIndex);

        switch (columnIndex){
            case 0:
                return callbacks.getToolName(logEntry.tool);
            case 1:
                return logEntry.url.toString();
            case 2:
                return logEntry.match;
            case 3:
                return logEntry.regex;
            default:
                return "";
        }
    }

    //重写ITab的方法
    //burp菜单栏的名字
    @Override
    public String getTabCaption() {
        return "regexkey";
    }
    //要展示的ui总布局
    @Override
    public Component getUiComponent() {
        return splitPane;
    }



    private class Table extends JTable{
        public Table(TableModel tableModel){
            super(tableModel);
        }

        @Override
        public void changeSelection(int row,int col,boolean toggle,boolean extend){
            LogEntry logEntry=log.get(row);
            requestViewer.setMessage(logEntry.requestResponse.getRequest(),true);
            responseViewer.setMessage(logEntry.requestResponse.getResponse(),false);
            currentlyDisplayedItem=logEntry.requestResponse;
            super.changeSelection(row,col,toggle,extend);
        }
    }

    private static class LogEntry{
        final int tool;
        final IHttpRequestResponsePersisted requestResponse;
        final URL url;
        final List<String> match;
        final String regex;
        LogEntry(int tool, IHttpRequestResponsePersisted requestResponse, URL url,List<String> match,String regex){
            this.tool=tool;
            this.requestResponse=requestResponse;
            this.url=url;
            this.match=match;
            this.regex=regex;
        }
    }

}
