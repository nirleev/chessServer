package chess.model;

public class MessageModel {

    public String msg;

    public MessageModel(String source, String msg){
        this.msg = msg;
    }

    public void setMsg(String msg){
        this.msg = msg;
    }

    public String getMsg(){
        return this.msg;
    }

}
