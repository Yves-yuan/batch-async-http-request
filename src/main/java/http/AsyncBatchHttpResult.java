package http;

import java.util.Vector;

public class AsyncBatchHttpResult {
    private  Vector<String> data;
    private  Vector<ErrorRequest> errors;

    AsyncBatchHttpResult(Vector<String> d,Vector<ErrorRequest> e){
        data = d;
        errors = e;
    }
    public Vector<String> getData() {
        return data;
    }

    public void setData(Vector<String> data) {
        this.data = data;
    }

    public Vector<ErrorRequest> getErrors() {
        return errors;
    }

    public void setErrors(Vector<ErrorRequest> errors) {
        this.errors = errors;
    }
}
