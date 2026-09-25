import java.util.*;

/** Java OOP concepts demo: abstraction, inheritance, polymorphism, interfaces,
 *  encapsulation, custom exceptions, collections, enums, streams, records. */
public class Main {

    enum TxType { DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT, INTEREST }

    record Transaction(int id, TxType type, double amount, double balanceAfter) {}

    static class InsufficientFundsException extends Exception {
        InsufficientFundsException(String msg) { super(msg); }
    }

    interface InterestBearing { double applyInterest(); }

    static abstract class Account {
        private static int nextId = 1000;                 // static: shared counter
        protected final int id;
        private final String owner;
        protected double balance;
        private final List<Transaction> history = new ArrayList<>();

        Account(String owner, double opening) {
            this.id = nextId++;
            this.owner = owner;
            this.balance = opening;
        }
        String getOwner() { return owner; }
        double getBalance() { return balance; }
        List<Transaction> getHistory() { return Collections.unmodifiableList(history); }

        protected void log(TxType t, double amt) {
            history.add(new Transaction(history.size() + 1, t, amt, balance));
        }
        void deposit(double amt) {
            if (amt <= 0) throw new IllegalArgumentException("Deposit must be positive");
            balance += amt;
            log(TxType.DEPOSIT, amt);
        }
        // template method: subclasses define what "available funds" means
        abstract double available();
        void withdraw(double amt) throws InsufficientFundsException {
            if (amt <= 0) throw new IllegalArgumentException("Withdrawal must be positive");
            if (amt > available())
                throw new InsufficientFundsException(
                    String.format("Account %d: need %.2f, available %.2f", id, amt, available()));
            balance -= amt;
            log(TxType.WITHDRAW, amt);
        }
        @Override public String toString() {
            return String.format("%s[id=%d, owner=%s, balance=%.2f]",
                    getClass().getSimpleName(), id, owner, balance);
        }
    }

    static class SavingsAccount extends Account implements InterestBearing {
        private final double rate;
        SavingsAccount(String owner, double opening, double rate) { super(owner, opening); this.rate = rate; }
        @Override double available() { return balance; }
        @Override public double applyInterest() {
            double i = balance * rate;
            balance += i;
            log(TxType.INTEREST, i);
            return i;
        }
    }

    static class CheckingAccount extends Account {
        private final double overdraft;
        CheckingAccount(String owner, double opening, double overdraft) { super(owner, opening); this.overdraft = overdraft; }
        @Override double available() { return balance + overdraft; }
    }

    static class Bank {
        private final Map<Integer, Account> accounts = new HashMap<>();
        <T extends Account> T open(T acc) { accounts.put(acc.id, acc); return acc; }
        void transfer(int from, int to, double amt) throws InsufficientFundsException {
            Account a = Optional.ofNullable(accounts.get(from))
                    .orElseThrow(() -> new NoSuchElementException("No account " + from));
            Account b = Optional.ofNullable(accounts.get(to))
                    .orElseThrow(() -> new NoSuchElementException("No account " + to));
            a.withdraw(amt);          // throws before anything changes -> atomic enough for demo
            b.deposit(amt);
        }
        double totalDeposits() { return accounts.values().stream().mapToDouble(Account::getBalance).sum(); }
        List<Account> richest(int n) {
            return accounts.values().stream()
                    .sorted(Comparator.comparingDouble(Account::getBalance).reversed())
                    .limit(n).toList();
        }
    }

    public static void main(String[] args) {
        Bank bank = new Bank();
        SavingsAccount s = bank.open(new SavingsAccount("Asha", 5000, 0.03));
        CheckingAccount c = bank.open(new CheckingAccount("Ben", 300, 200));

        s.deposit(250);
        System.out.println(s);
        System.out.printf("Interest credited: %.2f%n", s.applyInterest());

        try {
            c.withdraw(450);                       // uses overdraft
            System.out.println("Overdraft withdrawal OK -> " + c);
            c.withdraw(100);                       // exceeds
        } catch (InsufficientFundsException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        try {
            bank.transfer(s.id, c.id, 1000);
            System.out.println("After transfer: " + s + " | " + c);
        } catch (InsufficientFundsException e) {
            System.out.println("Transfer failed: " + e.getMessage());
        }

        // polymorphism: treat all accounts through the base type / interface
        for (Account a : List.of(s, c))
            System.out.printf("%-16s available=%.2f  interestBearing=%b%n",
                    a.getClass().getSimpleName(), a.available(), a instanceof InterestBearing);

        System.out.println("Total deposits: " + String.format("%.2f", bank.totalDeposits()));
        System.out.println("Richest: " + bank.richest(1));
        System.out.println("Savings history:");
        s.getHistory().forEach(t -> System.out.println("  " + t));
    }
}
