<!-- This is an example for a Svelte browser app, if you are using SvelteKit, please see the following example:
    https://codesandbox.io/p/sandbox/deep-chat-sveltekit-fn8h6x -->

        <script>
            import { DeepChat } from "deep-chat";
        </script>

<main>
    <h1>MariaDB documentation Chat</h1>
    <!-- demo/textInput are examples of passing an object directly into a property -->
    <deep-chat
        avatars="true"
        style="border-radius: 10px; width: 80vw; height: calc(100vh - 170px); font-size: 1.37rem; padding-top: 10px"
        textInput={{placeholder:{text: ""}}}
        connect={{websocket: true, url:"ws://localhost:80/chatbot"}}
        displayLoadingBubble="true"
    />

</main>

<style>
    main {
    font-family: sans-serif;
    text-align: center;
    justify-content: center;
    display: grid;
}
</style>