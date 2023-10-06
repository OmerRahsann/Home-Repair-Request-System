import React from "react";
import axios from "axios";
import { useNavigate } from 'react-router-dom';


function SignInForm() {
  const [state, setState] = React.useState({
    email: "",
    password: ""
  });
  const navigate = useNavigate();
  async function login(event) {
    event.preventDefault();
    try {
        const {email, password} = state;
        await axios.post("http://localhost:8080/api/login", {
            email: email,
            password: password,
        }).then((res) => {
            console.log(res.data);

            if (res.data.message == "Email does not exits") {
                alert("Email not exits");
            }
            else if (res.data.message == "Login Success") {

                navigate('/home');
            }
            else {
                alert("Incorrect Email or Email and Password do not match");
            }
        }, fail => {
            console.error(fail); // Error!
        });
    }

    catch (err) {
        alert(err);
    }

}
  const handleChange = evt => {
    const value = evt.target.value;
    setState({
      ...state,
      [evt.target.name]: value
    });
  };

  return (
    <div className="form-container sign-in-container">
      <form id="login" onSubmit={login}>
        <h1>Sign in</h1>
        <input
          type="email"
          placeholder="Email"
          name="email"
          value={state.email}
          onChange={handleChange}
        />
        <input
          type="password"
          name="password"
          placeholder="Password"
          value={state.password}
          onChange={handleChange}
        />
        <a href="#">Forgot your password?</a>
        <button>Sign In</button>
      </form>
    </div>
  );
}

export default SignInForm;
