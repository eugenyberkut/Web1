package com.homelinux.berkut.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Eugeny on 10.05.2015.
 * http://berkut.homelinux.com
 */
@javax.servlet.annotation.WebServlet(name = "StartTestServlet", urlPatterns = {"/teststart", "/testrun", "/testfinish", "/sendanswer"})
public class StartTestServlet extends javax.servlet.http.HttpServlet {
    List<Question> questionsList;

    @Override
    public void init() throws ServletException {
        super.init();
        questionsList = readBlock("q1.txt");
    }

    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        processRequest(request, response);
    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
//        String parameter = request.getParameter("test");
//        if (parameter != null) {
//            questionsList = readBlock(parameter+".txt");
//        }
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        switch (servletPath) {
            case "/teststart":
                starttest(request, response);
                break;
            case "/testrun":
                runtest(request, response);
                break;
            case "/testfinish":
                finishtest(request, response);
                break;
            case "/sendanswer":
                sendanswer(request, response);
            break;
            default: response.sendRedirect("index.jsp");
        }
    }

    private void sendanswer(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("questions")==null) {
            response.sendRedirect("index.jsp");
        } else {
            Integer current = (Integer) session.getAttribute("current");
            List<Question> questions = (List<Question>) session.getAttribute("questions");
            int numberQ = questions.size();
            String ans = request.getParameter("ans");
            if ("Next".equals(ans)) {
                current = (current + 1) % numberQ;
            }
            if ("Prev".equals(ans)) {
                current = (current + numberQ - 1) % numberQ;
            }
            if ("Accept".equals(ans)) {
                int[] answers = (int[]) session.getAttribute("answers");
                String q = request.getParameter("q");
                if (q != null) {
                    int a = Integer.parseInt(q);
                    answers[current] = a;
                    session.setAttribute("answers", answers);
                }
                current = (current + 1) % numberQ;
            }
            if ("Finish".equals(ans) && "1".equals(request.getParameter("finish"))) {
                int[] answers = (int[]) session.getAttribute("answers");
                String q = request.getParameter("q");
                if (q != null) {
                    int a = Integer.parseInt(q);
                    answers[current] = a;
                    session.setAttribute("answers", answers);
                }
                response.sendRedirect("testfinish");
            } else {
                session.setAttribute("current", current);
                response.sendRedirect("testrun");
            }
        }
    }

    private void finishtest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("questions")==null) {
            response.sendRedirect("index.jsp");
        } else {
            List<Question> questions = (List<Question>) session.getAttribute("questions");
            int[] answers = (int[]) session.getAttribute("answers");
            List<TriBean> result = new ArrayList<>();
            int totalCorrect = 0;
            for (int i = 0; i < answers.length; i++) {
                TriBean tb = new TriBean(i, answers[i], questions.get(i).getCorrect());
                if (answers[i] == questions.get(i).getCorrect()) {
                    totalCorrect++;
                }
                result.add(tb);
            }
            request.setAttribute("result", result);
            request.setAttribute("total", totalCorrect);
            request.setAttribute("wrong", answers.length-totalCorrect);
            request.setAttribute("fio", session.getAttribute("fio"));
            request.setAttribute("group", session.getAttribute("group"));
            request.setAttribute("starttime", session.getAttribute("starttime"));
            session.invalidate();
            request.getRequestDispatcher("/finish.jsp").forward(request, response);
        }
    }

    private void runtest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (session == null || session.isNew() || session.getAttribute("current") == null) {
            response.sendRedirect("index.jsp");
        } else {
            Integer current = (Integer)session.getAttribute("current");
            Integer answer = ((int[])session.getAttribute("answers"))[current];
            List<Question> questions = (List<Question>) session.getAttribute("questions");
            Question question = questions.get(current);
            String pict = question.getPicture();
            String picture = "";
            if (!pict.trim().isEmpty()){
                picture = "<img src=\"http://berkut.homelinux.com/download/test/img/"+pict+"\" /> <br/>";
            }
            request.setAttribute("picture", picture);
            AnswerVariant av = new AnswerVariant(question.getAnswers(), answer);
            request.setAttribute("av", av);
            request.setAttribute("q", question);
            request.getRequestDispatcher("/run.jsp").forward(request, response);
        }
    }

    private void starttest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fio = request.getParameter("fio");
        String group = request.getParameter("group");
        HttpSession session = request.getSession();
        List<Question> questions = createTest();
        session.setAttribute("questions", questions);
        session.setAttribute("group", group);
        session.setAttribute("fio", fio);
        session.setAttribute("starttime", new Date());
        session.setAttribute("current", 0);
        session.setAttribute("total", questions.size());
        int[] answers = new int[questions.size()];
        for (int i=0; i<answers.length; i++) {
            answers[i] = -1;
        }
        session.setAttribute("answers", answers);
        response.sendRedirect("testrun");
    }

    private List<Question> createTest() {
        List<Question> result = new ArrayList<>(questionsList);
        Collections.shuffle(result);
        return result.subList(0, 15);
    }

    private List<Question> readBlock(String fileName) {
        List<Question> block = new ArrayList<>();
        try (InputStream openStream = new URL("http://berkut.homelinux.com/download/test/"+fileName).openStream()) { // url
            BufferedReader in = new BufferedReader(new InputStreamReader(openStream, "cp1251")); // url
            String question;
            while ((question=in.readLine())!=null) {
                String pictureName = in.readLine();
                String addition = in.readLine();
                String[] answers = new String[4];
                List<String> ansList = new ArrayList<>(4);
                int correct = -1;
                for (int i=0; i<answers.length; i++) {
                    ansList.add(in.readLine());
                }
                Collections.shuffle(ansList);
                for (int i=0;i<answers.length; i++) {
                    String ans = ansList.get(i);
                    if (!ans.isEmpty() && ans.charAt(0)=='+') {
                        correct = i;
                        answers[i] = ans.substring(1);
                    } else answers[i] = ans;
                }
                Question q = new Question(question, pictureName, addition, answers, correct);
                block.add(q);
                in.readLine(); // пустая строка
            }
            return block;
        } catch (IOException ex) {
            Logger.getLogger(StartTestServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return block;
    }


}
