package org.telegram.ext.model;

public class IpApiResponse {

    private String query;

    public IpApiResponse() {
    }

    public IpApiResponse(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public String toString() {
        return "IpApiResponse{" +
                "query='" + query + '\'' +
                '}';
    }
}
