package jconverter;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class Converter {

    public Converter(List<Object> rawInput) {
        this.rawInput = rawInput;
    }

    private static final String deposit = "Deposit";

    private static final String withdraw = "Withdrawal";

    private static final String trade = "Trade";

    private record Input(Date date, String asset, String type, Double amount) {
    }

    public record Output(String date, String label, String outgoingAsset, String outgoingAmount,
                         String incomingAsset,
                         String incomingAmount, String comment) {
    }

    private final List<Object> rawInput;

    public List<ArrayList<String>> convert() {

        List<Output> outputList = new ArrayList<>();
        var inputList = new ArrayList<Input>();

        rawInput.forEach(seq -> {
            var row = (List<Object>) seq;
            inputList.add(new Input((Date) row.get(0), (String) row.get(1), (String) row.get(2), (Double) row.get(3)));
        });

        ListIterator<Input> it = inputList.listIterator();
        while (it.hasNext()) {
            Input input = it.next();
            var amount = BigDecimal.valueOf(input.amount).stripTrailingZeros().toString();
            final String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(input.date);
            switch (input.type) {
                case "deposit" -> outputList.add(new Output(date, deposit, "", "", input.asset, amount, "deposit"));
                case "withdraw" ->
                        outputList.add(new Output(date, withdraw, input.asset, replaceNegative(amount), "", "", "withdraw"));
                case "transferIn" ->
                        outputList.add(new Output(date, deposit, "", "", input.asset, amount, "transferIn"));
                case "transferOut" ->
                        outputList.add(new Output(date, withdraw, input.asset, replaceNegative(amount), "", "", "transferOut"));
                case "exchangeIn" -> {
                    Input nextInput = it.next();
                    assert nextInput.type.equals("exchangeOut");
                    var newRow = new Output(date, trade, nextInput.asset, replaceNegative(BigDecimal.valueOf(nextInput.amount).stripTrailingZeros().toString()), input.asset, amount, "exchangeIn/exchangeOut");
                    outputList.add(newRow);
                }
                case "exchangeOut" -> {
                }
                case "refund" -> outputList.add(new Output(date, deposit, "", "", input.asset, amount, "refund"));
                default -> throw new RuntimeException("unknown transaction type: " + input.type);
            }
        }

        return outputList.stream().map(output -> {
            var item = new ArrayList<String>();
            item.add(output.date);
            item.add(output.label);
            item.add(output.outgoingAsset);
            item.add(output.outgoingAmount);
            item.add(output.incomingAsset);
            item.add(output.incomingAmount);
            item.add(output.comment);
            return item;
        }).toList();
    }

    private String replaceNegative(String amount) {
        return amount.replaceFirst("-", "");
    }
}