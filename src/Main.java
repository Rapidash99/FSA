import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        Scanner input = new Scanner(new File("fsa.txt"));
        PrintWriter output = new PrintWriter("result.txt", "UTF-8");

        String[] inp = new String[5];  //putting every input string in array of strings
        for (int i = 0; i < 5; i ++) {
            inp[i] = input.nextLine();
        }

        FSAValidator validator = new FSAValidator(inp);  //creating FSA validator by using constructor
        output.print(validator.validate());  //printing validation result in result.txt
        output.close();
    }
}
