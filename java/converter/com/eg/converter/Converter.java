package com.eg.converter;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

enum Currency { DOLLAR, EURO, POUND }

class CModel extends Observable<CModel,CView,Currency> {
  private final EnumMap<Currency,Double> rates;
  private long value = 0; // cents, euro cents, or pence
  private Currency currency = Currency.DOLLAR;
  public CModel() {
    rates = new EnumMap<Currency,Double>(Currency.class);
  }
  public void initialize(double... initialRates) {
    for (int i=0; i<initialRates.length; i++)
      setRate(Currency.values()[i], initialRates[i]);
  }
  public void setRate(Currency currency, double rate) {
    rates.put(currency, rate);
    setChanged();
    notifyObservers(currency);
}
public void setValue(Currency currency, long value) {
  this.currency = currency;
  this.value = value;
  setChanged();
  notifyObservers(null);
}
public double getRate(Currency currency) {
  return rates.get(currency);
}
public long getValue(Currency currency) {
  if (currency == this.currency)
    return value;
  else
    return Math.round(value * getRate(currency) / getRate(this.currency));
  }
}
interface CView extends Observer<CModel,CView,Currency> {}

class RateView extends JTextField implements CView {
  private final CModel model;
  private final Currency currency;

  public RateView(final CModel model, final Currency currency) {
    this.model = model;
    this.currency = currency;
    addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          try {
            double rate = Double.parseDouble(getText());
            model.setRate(currency, rate);
          } catch (NumberFormatException x) {}
        }
      });
    model.addObserver(this);
  }

  public void update(CModel model, Currency currency) {
    if (this.currency == currency) {
      double rate = model.getRate(currency);
      setText(String.format("%10.6f", rate));
    }
  }
}

class ValueView extends JTextField implements CView {
  private final CModel model;
  private final Currency currency;

  public ValueView(final CModel model, final Currency currency) {
    this.model = model;
    this.currency = currency;
    addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          try {
            long value = Math.round(100.0*Double.parseDouble(getText()));
            model.setValue(currency, value);
          } catch (NumberFormatException x) {}
        }
      });
    model.addObserver(this);
  }

  public void update(CModel model, Currency currency) {
    if (currency == null || currency == this.currency) {
      long value = model.getValue(this.currency);
      setText(String.format("%15d.%02d", value/100, value%100));
    }
  }
}

class Converter extends JFrame {
  public Converter() {
    CModel model = new CModel();
    setTitle("Currency converter");
    setLayout(new GridLayout(Currency.values().length+1, 3));
    add(new JLabel("currency"));
    add(new JLabel("rate"));
    add(new JLabel("value"));
    for (Currency currency : Currency.values()) {
      add(new JLabel(currency.name()));
      add(new RateView(model, currency));
      add(new ValueView(model, currency));
    }
    model.initialize(1.0, 0.83, 0.56);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    pack();
  }
  public static void main(String[] args) {
    new Converter().setVisible(true);
  }
}
