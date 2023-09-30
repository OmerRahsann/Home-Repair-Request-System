import React from "react";
import axios from "axios";
function SignUpForm() {
  const [state, setState] = React.useState({
    userName: "",
    email: "",
    password: ""
  });
  const handleChange = evt => {
    const value = evt.target.value;
    setState({
      ...state,
      [evt.target.name]: value
    });
  };

  async function save(event) {
    event.preventDefault();
    try {
      const {userName, email, password} = state;
      await axios.post("http://localhost:8090/api/v1/user/save", {
        userName: userName,
        email: email,
        password: password,
      });
      alert("user Registation Successfully");
    } catch (err) {
      alert(err);
    }
  }

  return (
    <div className="form-container sign-up-container">
      <form onSubmit={save}>
        <h1>Create Account</h1>
        <input
          type="text"
          name="name"
          value={state.name}
          onChange={handleChange}
          placeholder="Name"
        />
        <input
          type="email"
          name="email"
          value={state.email}
          onChange={handleChange}
          placeholder="Email"
        />
        <input
          type="password"
          name="password"
          value={state.password}
          onChange={handleChange}
          placeholder="Password"
        />
        <button>Sign Up</button>
      </form>
    </div>
  );
}

export default SignUpForm;
