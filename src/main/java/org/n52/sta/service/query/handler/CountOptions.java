/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.query.handler;

import org.apache.olingo.server.api.uri.queryoption.CountOption;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class CountOptions {

    private int count;

    private boolean isCount;

    private CountOption countOption;

    public CountOption getCountOption() {
        return countOption;
    }

    public void setCountOption(CountOption countOption) {
        this.countOption = countOption;
    }

    public boolean isCount() {
        return isCount;
    }

    public void setIsCount(boolean isCount) {
        this.isCount = isCount;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
