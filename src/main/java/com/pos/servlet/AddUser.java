package com.pos.servlet;

import com.pos.bean.UserBean;
import com.pos.bean.RoleBean;
import com.pos.decorator.ConsoleDecorator;
import com.pos.entity.UserTable;
import com.pos.entity.Role;
import com.pos.utility.Notification;
import com.pos.utility.Password;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "AddUser", urlPatterns = {"/AddUser"})
public class AddUser extends HttpServlet {

    @Inject
    UserBean userBean;
    @Inject
    RoleBean roleBean;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String fullName = request.getParameter("fullname");
        String role = request.getParameter("role");
        String email = request.getParameter("email");
        String retypePass = request.getParameter("retypePass");

        List<Role> roles = roleBean.getAllRoles();

        boolean needsEdit = false;
        if (request.getParameter("action") != null) {
            needsEdit = request.getParameter("action").equals("edit");
        }
        System.out.println(request.getParameter("action"));
        int loggedAdminId = Integer.parseInt(request.getParameter("loggedUserId"));
        System.out.println("logged id is " + loggedAdminId);
        UserTable loggedUser = userBean.getById(loggedAdminId);
        
        request.setAttribute("loggedUser", loggedUser);

        if (needsEdit) {
            int userIdToUpdate = Integer.parseInt(request.getParameter("userId"));

            UserTable userToUpdate = userBean.getById(userIdToUpdate);

            if (username.equals(userToUpdate.getUsername())) {
                username = null;
            } else {
                try {
                    UserTable someUser = userBean.getByUsername(username);
                    request.setAttribute("err_msg_user", "User Taken");
                    request.setAttribute("roles", roles);
                    request.setAttribute("user", userToUpdate);

                    request.getRequestDispatcher("/WEB-INF/pages/editUser.jsp").forward(request, response);
                } catch (Exception ex) {
                    
                }
            }

            if (!password.equals("******")) {
                if (!password.equals(retypePass)) {
                    request.setAttribute("err_msg_pass", "Passwords do not match");
                    request.setAttribute("roles", roles);
                    request.setAttribute("user", userToUpdate);

                    request.getRequestDispatcher("/WEB-INF/pages/editUser.jsp").forward(request, response);
                } else {
                    password = Password.convertToSha256(password);
                }
            } else {
                password = null;
            }
            
            userBean.updateUser(userToUpdate, username, password, fullName, role, email, null);
            response.sendRedirect("http://localhost:8080/TheFinalPOS/View?userId=" + loggedAdminId);

            return;
        }

        UserTable user = null;

        if (!password.equals(retypePass)) {
            request.setAttribute("err_msg_pass", "Passwords do not match");
            request.setAttribute("roles", roles);
            request.getRequestDispatcher("/WEB-INF/pages/register.jsp").forward(request, response);
            return;
        }

        try {
            user = userBean.getByUsername(username);
            request.setAttribute("err_msg_user", "User Taken");
            request.setAttribute("roles", roles);
            request.getRequestDispatcher("/WEB-INF/pages/register.jsp").forward(request, response);
        } catch (Exception ex) {
            userBean.CreateUser(username, password, fullName, role, email);

            if(role.equals("Cashier")){
                Notification.notificationCount++;
            }
            
            if (Notification.events != null) {
                Notification.events.notify("New registered user is pending approval...");
            }

            if (Notification.decoratorEvents == null) {
                Notification.decoratorEvents = new ConsoleDecorator();
                Notification.decoratorEvents.notify("New registered user is pending approval...");
            }

            response.sendRedirect("http://localhost:8080/TheFinalPOS/View?userId=" + loggedUser.getId());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
