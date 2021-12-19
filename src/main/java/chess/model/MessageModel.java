package chess.model;

public class MessageModel {

    public String source;

    public String msg;

    public MessageModel(String source, String msg){
        this.source = source;
        this.msg = msg;
    }

    public void setMsg(String msg){
        this.msg = msg;
    }

    public String getMsg(){
        return this.msg;
    }

    public void setSource(String source){
        this.source = source;
    }

    public String getSource(){
        return this.source;
    }

}
