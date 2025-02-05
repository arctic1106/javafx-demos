public class ArgumentsEcho {

    public static void main(String[] args) {
        System.out.println("ArgEcho - Command Line Arguments Test");
        System.out.println("Number of arguments: " + args.length);

        if (args.length == 0)
            System.out.println("No arguments provided.");
        else {
            System.out.println("Arguments received:");
            for (int i = 0; i < args.length; i++)
                System.out.println("  arg[" + i + "]: " + args[i]);
        }

        System.out.println("\nTesting real-time output:");
        for (int i = 1; i <= 3; i++) {
            System.out.println("Output line " + i);
            System.out.flush();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Real-time output test complete!");
    }
}
