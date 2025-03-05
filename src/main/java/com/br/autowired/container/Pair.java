// NOTE: Using this Pair for now to store persistent lifecycle methods.
// By persistent methods i mean: PreDestroy, PreShutdown and Postshutdown.
// i treat them as persistent because they are stored, but are only executed when
// things are about to shutdown. So we "define" them early, but use only later.

package com.br.autowired.container;

public class Pair<L, R> {
  private L l;
  private R r;

  public Pair(L l, R r) {
    this.l = l;
    this.r = r;
  }

  public L getL() {
    return l;
  }

  public void setL(L l) {
    this.l = l;
  }

  public R getR() {
    return r;
  }

  public void setR(R r) {
    this.r = r;
  }
}
