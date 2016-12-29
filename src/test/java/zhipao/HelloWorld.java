package zhipao;

public class HelloWorld{
	public static void main(String []args){

		if (args.length!=2) {

			System.out.println("您输入的参数个数有误，必须是2个参数，分别表示指令和数值，请重新输入！");
		} else if(args[0].equals("Greetings")){
			System.out.println(args[1] + "你好，欢迎来到爪洼岛");
		} else if (args[0].equals("Reverse")){

			System.out.println(reverse2(args[1]));
			
		} else {
			System.out.println("您输入的指令有误");
			System.out.println("Greetings指令--输出欢迎信息");
			System.out.println("Reverse指令--反转字符");

		}
	}


	public static String reverse(String s){
		int length = s.length();
		String reverse = "";
		for(int i = 0; i < length; i++)
			reverse = s.charAt(i) + reverse;
		return reverse; 
	}

	public static String reverse2(String s){
		StringBuffer str = new StringBuffer(s); 
		int length = s.length();
		for(int i = 0; i < Math.floor(length/2); i++){
			char j = s.charAt(i);
			str.setCharAt(i,s.charAt(length-i-1));
			str.setCharAt(length-i-1, j);
		}
		return str.toString(); 
	}



}