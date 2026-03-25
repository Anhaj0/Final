import Button from "../components/Button";

export default function Login() {
  return (
    <div className="login-page">
      <h1>Login</h1>
      <form>
        <input type="text" placeholder="Username" className="input-field" />
        <input type="password" placeholder="Password" className="input-field" />
        <Button text="Login" />
      </form>
    </div>
  );
}
